package com.hyapp.restful.demo.controller;

import com.hyapp.restful.demo.common.Event;
import com.hyapp.restful.demo.common.ResultModel;
import com.hyapp.restful.demo.common.Status;
import com.hyapp.restful.demo.entity.GameResult;
import com.hyapp.restful.demo.entity.Player;
import com.hyapp.restful.demo.service.GameService;
import com.hyapp.restful.demo.utils.Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Wang Zhen
 * @date 2020/7/24 2:01 下午
 */
@Api(tags = "游戏接口")
@RestController
@RequestMapping("/game")
public class GameController {

    /**
     * 根据unionId映射用户信息
     */
    private Map<String, Player> userToPlayer = new ConcurrentHashMap<>();
    /**
     * 根据unionId映射用户状态
     */
    private Map<String, Status> userToStatus = new ConcurrentHashMap<>();
    /**
     * 根据roomID映射房间内所有用户的userID
     */
    private Map<String, List<String>> userToRoom = new ConcurrentHashMap<>();
    /**
     * 根据用户id映射游戏结果
     */
    private Map<String, Integer> userToResult = new ConcurrentHashMap<>();
    private static final int MAX_PEOPLE = 2;

    @Autowired
    private GameService gameService;

    @Autowired
    private Util util;

    @ApiOperation(value = "创建房间")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResultModel<Integer> createRoom(@RequestParam(value = "unionId") String unionId,
                                           @RequestParam(value = "nickName") String nickName,
                                           @RequestParam(value = "picUrl") String picUrl) {
        ResultModel<Integer> result = new ResultModel<>();
        try {
            userToStatus.put(unionId, Status.IN_ROOM);
            result = gameService.createRoom(unionId);
            userToRoom.put(String.valueOf(result.getResult()), new ArrayList<>());
            userToRoom.get(String.valueOf(result.getResult())).add(unionId);
            Player player = new Player();
            player.setUnionId(unionId);
            player.setNickName(nickName);
            player.setPicUrl(picUrl);
            userToPlayer.put(unionId, player);
            return result;
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @ApiOperation(value = "加入游戏")
    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public ResultModel<String> joinGame(@RequestParam(value = "roomID") String roomID,
                                        @RequestParam(value = "unionId") String unionId,
                                        @RequestParam(value = "nickName") String nickName,
                                        @RequestParam(value = "picUrl") String picUrl) {
        ResultModel<String> result = new ResultModel<>();
        try {
            userToStatus.put(unionId, Status.IN_ROOM);
            if (userToRoom.get(roomID).size() >= MAX_PEOPLE) {
                return new ResultModel<String>().sendFailedMessage("人数已满，无法加入！");
            }
            userToRoom.get(roomID).add(unionId);
            Player player = new Player();
            player.setUnionId(unionId);
            player.setNickName(nickName);
            player.setPicUrl(picUrl);
            userToPlayer.put(unionId, player);
            List<Player> playerList = new ArrayList<>();
            for (String user : userToRoom.get(roomID)) {
                playerList.add(userToPlayer.get(user));
            }
            for (String user : userToRoom.get(roomID)) {
                util.postEventAndMessageByProfileId(user, Event.JOIN.getEvent(), playerList.toString());
            }
            return result.sendSuccessResult("加入成功！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @ApiOperation(value = "离开房间")
    @RequestMapping(value = "/leave", method = RequestMethod.POST)
    public ResultModel<String> leaveGame(@RequestParam(value = "roomID") String roomID,
                                         @RequestParam(value = "unionId") String unionId) {
        ResultModel<String> result = new ResultModel<>();
        try {
            userToStatus.put(unionId, Status.IN_ROOM);
            List<String> userInRoom = userToRoom.get(roomID);
            userInRoom.remove(unionId);
            userToPlayer.remove(unionId);
            List<Player> playerList = new ArrayList<>();
            for (String user : userInRoom) {
                playerList.add(userToPlayer.get(user));
            }
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.LEAVE.getEvent(), playerList.toString());
            }
            return result.sendSuccessResult("离开游戏！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @ApiOperation(value = "玩家准备")
    @RequestMapping(value = "/status/ready", method = RequestMethod.POST)
    public ResultModel<String> setReady(@RequestParam(value = "roomID") String roomID,
                         @RequestParam(value = "unionId") String unionId) {
        ResultModel<String> result = new ResultModel<>();
        try {
            userToStatus.put(unionId, Status.READY);
            List<String> userInRoom = userToRoom.get(roomID);
            Map<String, Status> playerStatus = new HashMap<>();
            for (String user : userInRoom) {
                playerStatus.put(user, userToStatus.get(user));
            }
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.READY.getEvent(), playerStatus.toString());
            }
            for (String user : userInRoom) {
                if (userToStatus.get(user) != Status.READY) {
                    return result.sendSuccessResult("已准备！");
                }
            }
            for (String user : userInRoom) {
                userToStatus.put(user, Status.IN_GAME);
            }
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.Start.getEvent(), playerStatus.toString());
            }
            return result.sendSuccessResult("已准备,游戏马上开始！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @ApiOperation(value = "玩家取消准备")
    @RequestMapping(value = "/status/unready", method = RequestMethod.POST)
    public ResultModel<String> setUnready(@RequestParam(value = "roomID") String roomID,
                           @RequestParam(value = "unionId") String unionId) {
        ResultModel<String> result = new ResultModel<>();
        try {
            userToStatus.put(unionId, Status.IN_ROOM);
            List<String> userInRoom = userToRoom.get(roomID);
            Map<String, Status> playerStatus = new HashMap<>();
            for (String user : userInRoom) {
                playerStatus.put(user, userToStatus.get(user));
            }
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.UNREADY.getEvent(), playerStatus.toString());
            }
            return result.sendSuccessResult("取消准备！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @ApiOperation(value = "玩家游戏结束")
    @RequestMapping(value = "/status/finish", method = RequestMethod.POST)
    public ResultModel<String> finishGame(@RequestParam(value = "roomID") String roomID,
                                          @RequestParam(value = "unionId") String unionId,
                                          @RequestParam(value = "score") int score) {
        ResultModel<String> result = new ResultModel<>();
        try {
            userToStatus.put(unionId, Status.FINISH);
            userToResult.put(unionId, score);
            Map<String, Integer> userInRoomToGameResult = new HashMap<>();
            List<String> userInRoom = userToRoom.get(roomID);
            for (String user : userInRoom) {
                if (userToStatus.get(user) != Status.FINISH) {
                    return result.sendSuccessResult("完成游戏，请等待游戏结果！");
                }
                userInRoomToGameResult.put(user, userToResult.get(user));
            }
            for (String user : userInRoom) {
                userToStatus.put(user, Status.IN_ROOM);
            }
            GameResult gameResult = gameService.getGameResult(userInRoom, userInRoomToGameResult);
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.FINISH.getEvent(), gameResult.toString());
            }
            return result.sendSuccessResult("完成游戏，请等待游戏结果！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }
}

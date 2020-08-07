package com.hyapp.restful.demo.service;


import com.alibaba.fastjson.JSONObject;
import com.hyapp.restful.demo.common.Event;
import com.hyapp.restful.demo.common.ResultModel;
import com.hyapp.restful.demo.common.Status;
import com.hyapp.restful.demo.entity.GameResult;
import com.hyapp.restful.demo.entity.Player;
import com.hyapp.restful.demo.entity.Room;
import com.hyapp.restful.demo.mapper.GameMapper;
import com.hyapp.restful.demo.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Wang Zhen
 * @date 2020/7/24 1:49 下午
 */
@Service
@Slf4j
public class GameServiceImpl implements GameService {

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
    private Map<Integer, List<String>> userToRoom = new ConcurrentHashMap<>();
    /**
     * 根据用户id映射游戏结果
     */
    private Map<String, Integer> userToResult = new ConcurrentHashMap<>();
    /**
     * 房间人数最大值
     */
    private static final int MAX_PEOPLE = 2;

    @Autowired
    GameMapper gameMapper;

    @Autowired
    private Util util;

    private Integer getRoomID(String unionId) {
        try {
            Room room = new Room();
            room.setCreateUserID(unionId);
            gameMapper.insertRoom(room);
            return room.getId();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public ResultModel<Integer> createRoom(String unionId, String nickName, String picUrl) {
        ResultModel<Integer> result = new ResultModel<>();
        try {
            userToStatus.put(unionId, Status.IN_ROOM);
            int roomID = this.getRoomID(unionId);
            userToRoom.put(roomID, new ArrayList<>());
            userToRoom.get(roomID).add(unionId);
            Player player = new Player();
            player.setUnionId(unionId);
            player.setNickName(nickName);
            player.setPicUrl(picUrl);
            userToPlayer.put(unionId, player);
            return result.sendSuccessResult(roomID);
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @Override
    public ResultModel<String> joinGame(Integer roomID, String unionId, String nickName, String picUrl) {
        ResultModel<String> result = new ResultModel<>();
        try {
            if(!userToRoom.containsKey(roomID)) {
                return result.sendFailedMessage("房间不存在！");
            }
            userToStatus.put(unionId, Status.IN_ROOM);
            if (userToRoom.get(roomID).size() >= MAX_PEOPLE) {
                return result.sendFailedMessage("人数已满，无法加入！");
            }
            userToRoom.get(roomID).add(unionId);
            Player player = new Player();
            player.setUnionId(unionId);
            player.setNickName(nickName);
            player.setPicUrl(picUrl);
            userToPlayer.put(unionId, player);
            List<Player> playerList = new ArrayList<>();
            List<String> userInRoom = userToRoom.get(roomID);
            for (String user : userInRoom) {
                playerList.add(userToPlayer.get(user));
            }
            for (String profileId : userInRoom) {
                util.postEventAndMessageByProfileId(profileId, Event.JOIN.getEvent(), playerList.toString());
            }
            return result.sendSuccessResult("加入成功！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @Override
    public ResultModel<String> leaveGame(Integer roomID, String unionId) {
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
            if(userInRoom.size()==0) {
                userToRoom.remove(roomID);
            }
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.LEAVE.getEvent(), playerList.toString());
            }
            return result.sendSuccessResult("离开游戏！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @Override
    public ResultModel<String> setReady(Integer roomID, String unionId) {
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
                util.postEventAndMessageByProfileId(user, Event.START.getEvent(), playerStatus.toString());
            }
            return result.sendSuccessResult("已准备,游戏马上开始！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @Override
    public ResultModel<String> setUnready(Integer roomID, String unionId) {
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

    @Override
    public ResultModel<String> finishGame(Integer roomID, String unionId, Integer score) {
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
            GameResult gameResult = this.getGameResult(userInRoom, userInRoomToGameResult);
            gameMapper.insertGameResult(JSONObject.toJSONString(gameResult), roomID);
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.FINISH.getEvent(), gameResult.toString());
            }
            return result.sendSuccessResult("完成游戏，请等待游戏结果！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    private GameResult getGameResult(List<String> userInRoom, Map<String, Integer> userInRoomToGameResult) {
        try {
            GameResult gameResult = new GameResult();
            List<Player> playerList = new ArrayList<>();
            Player winner = null;
            int maxResult = userInRoomToGameResult.get(userInRoom.get((0)));
            boolean equal = false;
            for(String user: userInRoom) {
                Player player = new Player();
                player.setUnionId(user);
                int personalResult = userInRoomToGameResult.get(user);
                player.setGameResult(personalResult);
                playerList.add(player);
                if(winner==null) {
                    winner = player;
                } else if(personalResult>maxResult) {
                    winner = player;
                } else if(personalResult==maxResult) {
                    equal = true;
                }
            }
            gameResult.setWinner(winner);
            gameResult.setEqual(equal);
            gameResult.setPlayerList(playerList);
            Random rand = new Random();
            gameResult.setPunishment(rand.nextInt(8));
            return gameResult;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public ResultModel<String> chooseCirclePunishment(Integer roomID, Integer punishmentID) {
        ResultModel<String> result = new ResultModel<>();
        try {
            List<String> userInRoom = userToRoom.get(roomID);
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.RESULT.getEvent(), String.valueOf(punishmentID));
            }
            return result.sendSuccessResult("选择转盘惩罚成功！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @Override
    public ResultModel<String> choosePersonalizedPunishment(Integer roomID, String punishment) {
        ResultModel<String> result = new ResultModel<>();
        try {
            List<String> userInRoom = userToRoom.get(roomID);
            for (String user : userInRoom) {
                util.postEventAndMessageByProfileId(user, Event.RESULT.getEvent(), punishment);
            }
            return result.sendSuccessResult("选择自定义惩罚成功！");
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }
}

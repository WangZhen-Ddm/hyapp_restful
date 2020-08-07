package com.hyapp.restful.demo.controller;

import com.hyapp.restful.demo.common.ResultModel;
import com.hyapp.restful.demo.service.GameService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Wang Zhen
 * @date 2020/7/24 2:01 下午
 */
@Api(tags = "游戏接口")
@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @ApiOperation(value = "创建房间")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResultModel<Integer> createRoom(@RequestParam(value = "unionId") String unionId,
                                           @RequestParam(value = "nickName") String nickName,
                                           @RequestParam(value = "picUrl") String picUrl) {
        return gameService.createRoom(unionId, nickName, picUrl);
    }

    @ApiOperation(value = "加入游戏")
    @RequestMapping(value = "/join", method = RequestMethod.POST)
    public ResultModel<String> joinGame(@RequestParam(value = "roomID") Integer roomID,
                                        @RequestParam(value = "unionId") String unionId,
                                        @RequestParam(value = "nickName") String nickName,
                                        @RequestParam(value = "picUrl") String picUrl) {
        return gameService.joinGame(roomID, unionId, nickName, picUrl);
    }

    @ApiOperation(value = "离开房间")
    @RequestMapping(value = "/leave", method = RequestMethod.POST)
    public ResultModel<String> leaveGame(@RequestParam(value = "roomID") Integer roomID,
                                         @RequestParam(value = "unionId") String unionId) {
        return gameService.leaveGame(roomID, unionId);
    }

    @ApiOperation(value = "玩家准备")
    @RequestMapping(value = "/status/ready", method = RequestMethod.POST)
    public ResultModel<String> setReady(@RequestParam(value = "roomID") Integer roomID,
                         @RequestParam(value = "unionId") String unionId) {
        return gameService.setReady(roomID, unionId);
    }

    @ApiOperation(value = "玩家取消准备")
    @RequestMapping(value = "/status/unready", method = RequestMethod.POST)
    public ResultModel<String> setUnready(@RequestParam(value = "roomID") Integer roomID,
                           @RequestParam(value = "unionId") String unionId) {
        return gameService.setUnready(roomID, unionId);
    }

    @ApiOperation(value = "玩家游戏结束")
    @RequestMapping(value = "/status/finish", method = RequestMethod.POST)
    public ResultModel<String> finishGame(@RequestParam(value = "roomID") Integer roomID,
                                          @RequestParam(value = "unionId") String unionId,
                                          @RequestParam(value = "score") int score) {
        return gameService.finishGame(roomID, unionId, score);
    }

    @ApiOperation(value = "转盘惩罚")
    @RequestMapping(value = "/punishment/circle", method = RequestMethod.POST)
    public ResultModel<String> chooseCirclePunishment(@RequestParam(value = "roomID") Integer roomID,
                                                      @RequestParam(value = "punishmentID") Integer punishmentID) {
        return gameService.chooseCirclePunishment(roomID, punishmentID);
    }

    @ApiOperation(value = "自定义惩罚")
    @RequestMapping(value = "/punishment/personalized", method = RequestMethod.POST)
    public ResultModel<String> choosePersonalizedPunishment(@RequestParam(value = "roomID") Integer roomID,
                                                            @RequestParam(value = "punishment") String punishment) {
        return gameService.choosePersonalizedPunishment(roomID, punishment);
    }
}

package com.hyapp.restful.demo.service;

import com.hyapp.restful.demo.common.ResultModel;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Wang Zhen
 * @date 2020/7/24 1:50 下午
 */
public interface GameService {

    ResultModel<Integer> createRoom(String unionId, String nickName, String picUrl);

    ResultModel<String> joinGame(Integer roomID, String unionId, String nickName, String picUrl);

    ResultModel<String> leaveGame(Integer roomID, String unionId);

    ResultModel<String> setReady(Integer roomID, String unionId);

    ResultModel<String> setUnready(Integer roomID, String unionId);

    ResultModel<String> finishGame(Integer roomID, String unionId, Integer score);

    ResultModel<String> chooseCirclePunishment(Integer roomID, Integer punishmentID);

    ResultModel<String> choosePersonalizedPunishment(Integer roomID, String punishment);
}

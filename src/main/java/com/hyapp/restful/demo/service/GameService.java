package com.hyapp.restful.demo.service;

import com.hyapp.restful.demo.common.ResultModel;
import com.hyapp.restful.demo.entity.GameResult;

import java.util.List;
import java.util.Map;

/**
 * @author Wang Zhen
 * @date 2020/7/24 1:50 下午
 */
public interface GameService {

    ResultModel<Integer> createRoom(String createUserID);

    GameResult getGameResult(List<String> userInRoom, Map<String, Integer> userInRoomToGameResult);
}

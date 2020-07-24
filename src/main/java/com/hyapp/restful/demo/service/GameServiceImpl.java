package com.hyapp.restful.demo.service;


import com.hyapp.restful.demo.common.ResultModel;
import com.hyapp.restful.demo.entity.GameResult;
import com.hyapp.restful.demo.entity.Player;
import com.hyapp.restful.demo.entity.Room;
import com.hyapp.restful.demo.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Wang Zhen
 * @date 2020/7/24 1:49 下午
 */
@Service
public class GameServiceImpl implements GameService {

    @Autowired
    GameMapper gameMapper;

    @Override
    public ResultModel<Integer> createRoom(String createUserID) {
        ResultModel<Integer> result = new ResultModel<>();
        try {
            Room room = new Room();
            room.setCreateUserID(createUserID);
            gameMapper.insertRoom(room);
            return result.sendSuccessResult(room.getId());
        } catch (Exception e) {
            return result.sendFailedMessage(e.getMessage());
        }
    }

    @Override
    public GameResult getGameResult(List<String> userInRoom, Map<String, Integer> userInRoomToGameResult) {
        GameResult result = new GameResult();
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
                    equal = false;
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
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}

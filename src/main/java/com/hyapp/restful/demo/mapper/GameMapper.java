package com.hyapp.restful.demo.mapper;

import com.hyapp.restful.demo.entity.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Wang Zhen
 * @date 2020/7/23 3:18 下午
 */
@Mapper
@Repository
public interface GameMapper {

    @Insert("insert into room (creatorUnionId) values (#{creatorUnionId})")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    void insertRoom(Room room);

    @Insert("insert into game_result (unionId, winner, playerList, equal) values (#{unionId}, #{winner}, #{playerList}, #{equal})")
    void insertGameResult(String unionId, String winner, String playerList, boolean equal);

    @Insert("insert into game_result_single (unionId, score) values (#{unionId}, #{score})")
    void insertSingleGameResult(String unionId, int score);

    @Select("select winner, playerList, equal, createTime from game_result where unionId = #{unionId}")
    List<GameResultWithTime> selectGameResultByUnionId(String unionId);

    @Select("select score, createTime from game_result_single where unionId = #{unionId}")
    List<SingleGameResultWithTime> selectSingleGameResultByUnionId(String unionId);
}

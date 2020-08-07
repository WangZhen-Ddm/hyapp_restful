package com.hyapp.restful.demo.mapper;

import com.hyapp.restful.demo.entity.GameResult;
import com.hyapp.restful.demo.entity.Room;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

/**
 * @author Wang Zhen
 * @date 2020/7/23 3:18 下午
 */
@Mapper
@Repository
public interface GameMapper {

    @Insert("insert into room (createUserID) values (#{createUserID})")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    void insertRoom(Room room);

    @Insert("insert into room (gameResult) values (#{gameResult}) where id = (#{roomID})")
    void insertGameResult(GameResult gameResult, Integer roomID);
}

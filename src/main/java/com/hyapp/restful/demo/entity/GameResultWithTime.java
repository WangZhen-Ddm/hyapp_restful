package com.hyapp.restful.demo.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Wang Zhen
 * @date 2020/8/21 3:56 下午
 */
@Data
public class GameResultWithTime {

    private String unionId;

    private Integer winnerIndex;

    private String playerList;

    private Boolean equal;

    private Date createTime;

}

package com.hyapp.restful.demo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Wang Zhen
 * @date 2020/8/21 3:59 下午
 */
@Data
public class SingleGameResultWithTime {

    private Integer score;

    private Date createTime;
}

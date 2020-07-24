package com.hyapp.restful.demo.common;

/**
 * @author Wang Zhen
 * @date 2020/7/24 11:28 上午
 */
public enum Event {
    JOIN("加入房间"),
    LEAVE("离开房间"),
    READY("准备"),
    UNREADY("取消准备"),
    Start("游戏开始"),
    FINISH("游戏结束");

    private String event;

    private Event(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}

package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

import java.util.List;

@Data
public class RoundEndDTO {
    /**
     * 房间号
     */
    private String roomId;

    /**
     * 用户ID
     */
    private int userId;

    /**
     * 卡牌名称列表
     */
    private List<String> cardsName;

    /**
     * 当前回合数
     */
    private int round;

    /**
     * 提交时间戳，用于处理潜在的超时或重复提交
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 是否已处理
     */
    private boolean processed = false;
}
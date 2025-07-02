package com.example.bg.feihua;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FeiHua {
    private Integer id;
    private Integer userId;        // 可为NULL，支持匿名游戏
    private String playerName;     // 玩家名称
    private Integer score;         // 游戏分数
    private String mode;          // 游戏模式: 'endless', 'challenge'
    private String difficulty;    // 难度: 'easy', 'normal', 'hard'
    private String keywordsUsed;  // 使用的关键词，逗号分隔
    private LocalDate playDate;   // 游戏日期
    private LocalDateTime createdAt; // 创建时间
}
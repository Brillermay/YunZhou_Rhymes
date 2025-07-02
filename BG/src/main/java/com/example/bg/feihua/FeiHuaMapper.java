package com.example.bg.feihua;

import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FeiHuaMapper {
    
    // 插入游戏记录
    void insertGameRecord(FeiHua record);
    
    // 获取今日挑战人数
    Integer getTodayPlayerCount(@Param("date") LocalDate date);
    
    // 获取平均分数
    Double getAverageScore();
    
    // 获取总游戏次数
    Integer getTotalGames();
    
    // 获取排行榜
    List<Map<String, Object>> getLeaderboard(@Param("limit") int limit);
    
    // 获取用户排名
    Integer getUserRank(@Param("userId") Integer userId);
    
    // 获取用户最佳分数
    Integer getUserBestScore(@Param("userId") Integer userId);
    
    // 获取用户总游戏次数
    Integer getUserTotalGames(@Param("userId") Integer userId);
    
    // 更新用户最佳分数
    void updateUserBestScore(@Param("userId") Integer userId, @Param("score") Integer score);
    
    // 增加用户游戏次数
    void incrementUserGames(@Param("userId") Integer userId);
    
    // 更新用户成就
    void updateUserAchievements(@Param("userId") Integer userId, @Param("achievements") String achievements);
}
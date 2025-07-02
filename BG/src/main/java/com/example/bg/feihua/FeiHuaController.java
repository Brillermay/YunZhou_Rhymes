package com.example.bg.feihua;

import com.example.bg.ConnetMySQL;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "飞花令游戏", description = "飞花令游戏相关接口")
@RestController
@RequestMapping("/api/feihua")
@CrossOrigin(origins = "http://localhost:5173")
public class FeiHuaController extends ConnetMySQL {

    /**
     * API 1: 获取游戏统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取飞花令游戏统计数据")
    public Map<String, Object> getGameStats() {
        Map<String, Object> result = new HashMap<>();
        
        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {
            
            FeiHuaMapper mapper = session.getMapper(FeiHuaMapper.class);
            
            // 获取今日挑战人数
            Integer todayPlayers = mapper.getTodayPlayerCount(LocalDate.now());
            
            // 获取平均分数
            Double averageScore = mapper.getAverageScore();
            
            // 获取总游戏次数
            Integer totalGames = mapper.getTotalGames();
            
            // 计算通关率 (假设10分以上为通关)
            Double successRate = averageScore != null ? (averageScore > 10 ? 75.0 : 45.0) : 0.0;
            
            result.put("success", true);
            result.put("data", Map.of(
                "todayPlayers", todayPlayers != null ? todayPlayers : 0,
                "averageScore", averageScore != null ? averageScore.intValue() : 0,
                "totalGames", totalGames != null ? totalGames : 0,
                "successRate", successRate,
                "averageTime", 45 // 假设平均用时45秒
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取统计数据失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * API 2: 获取用户成就徽章
     */
    @GetMapping("/achievements/{userId}")
    @Operation(summary = "获取用户成就徽章")
    public Map<String, Object> getUserAchievements(@PathVariable Integer userId) {
        Map<String, Object> result = new HashMap<>();
        
        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {
            
            FeiHuaMapper mapper = session.getMapper(FeiHuaMapper.class);
            
            // 获取用户最佳分数
            Integer bestScore = mapper.getUserBestScore(userId);
            
            // 获取用户总游戏次数
            Integer totalGames = mapper.getUserTotalGames(userId);
            
            // 根据分数判断成就
            Map<String, Object> achievements = new HashMap<>();
            achievements.put("bronze", bestScore != null && bestScore >= 5);
            achievements.put("silver", bestScore != null && bestScore >= 15);
            achievements.put("gold", bestScore != null && bestScore >= 25);
            achievements.put("bestScore", bestScore != null ? bestScore : 0);
            achievements.put("totalGames", totalGames != null ? totalGames : 0);
            achievements.put("progress", bestScore != null ? Math.min(bestScore * 4, 100) : 0);
            
            result.put("success", true);
            result.put("data", achievements);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取成就数据失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * API 4: 获取实时排行榜
     */
    @GetMapping("/leaderboard")
    @Operation(summary = "获取飞花令排行榜")
    public Map<String, Object> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {
            
            FeiHuaMapper mapper = session.getMapper(FeiHuaMapper.class);
            
            // 获取排行榜
            List<Map<String, Object>> leaderboard = mapper.getLeaderboard(limit);
            
            result.put("success", true);
            result.put("data", Map.of(
                "leaderboard", leaderboard,
                "totalPlayers", mapper.getTotalGames()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取排行榜失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 提交游戏成绩
     */
    @PostMapping("/submit-score")
    @Operation(summary = "提交飞花令游戏成绩")
    public Map<String, Object> submitScore(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {
            
            FeiHuaMapper mapper = session.getMapper(FeiHuaMapper.class);
            
            // 创建游戏记录
            FeiHua record = new FeiHua();
            record.setUserId((Integer) request.get("userId"));
            record.setPlayerName((String) request.get("playerName"));
            record.setScore((Integer) request.get("score"));
            record.setMode((String) request.get("mode"));
            record.setDifficulty((String) request.get("difficulty"));
            record.setKeywordsUsed((String) request.get("keywordsUsed"));
            record.setPlayDate(LocalDate.now());
            
            // 插入记录
            mapper.insertGameRecord(record);
            
            // 如果是登录用户，更新用户数据
            Integer userId = record.getUserId();
            if (userId != null) {
                // 增加游戏次数
                mapper.incrementUserGames(userId);
                
                // 检查是否更新最佳分数
                Integer currentBest = mapper.getUserBestScore(userId);
                if (currentBest == null || record.getScore() > currentBest) {
                    mapper.updateUserBestScore(userId, record.getScore());
                }
            }
            
            session.commit();
            
            result.put("success", true);
            result.put("message", "成绩提交成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "提交成绩失败：" + e.getMessage());
        }
        
        return result;
    }
}
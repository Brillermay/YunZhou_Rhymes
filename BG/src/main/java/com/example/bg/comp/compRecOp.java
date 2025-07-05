package com.example.bg.comp;

import com.example.bg.ConnetMySQL;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/compRec")
public class compRecOp extends ConnetMySQL {

    @PostMapping("/submit")
    public String submitRecord(@RequestBody compRec rec) {
        SqlSession session = null;
        InputStream in = null;
        try {
            System.out.println("收到前端提交: " + rec);

            in = Resources.getResourceAsStream("SqlMapConfig.xml");
            System.out.println("成功加载SqlMapConfig.xml");

            session = getSession(in);
            System.out.println("成功获取数据库Session");

            compRecOpMapper mapper = session.getMapper(compRecOpMapper.class);

            System.out.println("开始查询历史记录...");
            compRec oldRec = mapper.getHistory(rec.UID, rec.Difficulty, rec.Sum);
            System.out.println("查询结果: " + oldRec);

            if (oldRec == null) {
                System.out.println("无历史记录，准备插入新纪录");
                mapper.addHistory(rec);
                session.commit();
                System.out.println("插入成功");
                return "Record added successfully";
            } else {
                // 修改逻辑：Max更大 或 Max相等且Mintime更小才更新
                if (rec.Max > oldRec.Max || (rec.Max == oldRec.Max && rec.Mintime < oldRec.Mintime)) {
                    System.out.println("新纪录更优，准备更新");
                    mapper.updateHistory(rec);
                    session.commit();
                    System.out.println("更新成功");
                    return "Record updated successfully";
                } else {
                    System.out.println("新纪录不优于历史记录，无需更新");
                    return "No update needed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) session.rollback();
            System.out.println("发生异常: " + e.getMessage());
            return "Error: " + e.getMessage();
        } finally {
            try {
                if (in != null) in.close();
                if (session != null) session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/rank")
    public Map<String, Object> getRank(@RequestBody compRec rec) {
        SqlSession session = null;
        InputStream in = null;
        try {
            in = Resources.getResourceAsStream("SqlMapConfig.xml");
            session = getSession(in);
            compRecOpMapper mapper = session.getMapper(compRecOpMapper.class);
            
            List<compRec> rankList = mapper.getRank(rec.Difficulty, rec.Sum);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rankList);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("data", null);
            response.put("message", e.getMessage());
            return response;
        } finally {
            if (in != null) try { in.close(); } catch (Exception e) {}
            if (session != null) try { session.close(); } catch (Exception e) {}
        }
    }

        // 🔧 新增方法：更新用户统计
    @PostMapping("/update-stats")
    public String updateUserStats(@RequestBody Map<String, Object> requestData) {
        SqlSession session = null;
        InputStream in = null;
        try {
            System.out.println("收到统计更新请求: " + requestData);

            int UID = (Integer) requestData.get("UID");
            int totalQuestions = (Integer) requestData.get("totalQuestions");
            int correctQuestions = (Integer) requestData.get("correctQuestions");

            in = Resources.getResourceAsStream("SqlMapConfig.xml");
            session = getSession(in);
            compRecOpMapper mapper = session.getMapper(compRecOpMapper.class);

            // 查询现有统计
            Map<String, Object> existingStats = mapper.getUserStats(UID);
            
            if (existingStats == null) {
                // 创建新统计记录
                Map<String, Object> newStats = new HashMap<>();
                newStats.put("UID", UID);
                newStats.put("totalQuestions", totalQuestions);
                newStats.put("correctQuestions", correctQuestions);
                newStats.put("accuracyRate", Math.round(correctQuestions * 100.0 / totalQuestions * 100) / 100.0);
                newStats.put("consecutiveDays", 1);
                newStats.put("lastStudyDate", LocalDate.now().toString());
                
                mapper.insertUserStats(newStats);
                System.out.println("创建新统计记录成功");
            } else {
                // 更新现有统计
                int newTotal = (Integer) existingStats.get("total_questions") + totalQuestions;
                int newCorrect = (Integer) existingStats.get("correct_questions") + correctQuestions;
                double newAccuracy = Math.round(newCorrect * 100.0 / newTotal * 100) / 100.0;
                
                // 🔧 修复：正确处理日期类型转换
                LocalDate lastStudyDate;
                Object lastStudyDateObj = existingStats.get("last_study_date");
                
                if (lastStudyDateObj == null) {
                    lastStudyDate = LocalDate.now().minusDays(1);
                } else if (lastStudyDateObj instanceof java.sql.Date) {
                    // 如果是 java.sql.Date 类型
                    lastStudyDate = ((java.sql.Date) lastStudyDateObj).toLocalDate();
                } else if (lastStudyDateObj instanceof String) {
                    // 如果是 String 类型
                    lastStudyDate = LocalDate.parse((String) lastStudyDateObj);
                } else {
                    // 其他类型，使用默认值
                    lastStudyDate = LocalDate.now().minusDays(1);
                }
                
                LocalDate today = LocalDate.now();
                
                int consecutiveDays;
                if (lastStudyDate.equals(today)) {
                    // 今天已经答过题，保持连续天数
                    consecutiveDays = (Integer) existingStats.get("consecutive_days");
                } else if (lastStudyDate.equals(today.minusDays(1))) {
                    // 昨天答过题，连续天数+1
                    consecutiveDays = (Integer) existingStats.get("consecutive_days") + 1;
                } else {
                    // 中断了，重新开始
                    consecutiveDays = 1;
                }
                
                Map<String, Object> updateStats = new HashMap<>();
                updateStats.put("UID", UID);
                updateStats.put("totalQuestions", newTotal);
                updateStats.put("correctQuestions", newCorrect);
                updateStats.put("accuracyRate", newAccuracy);
                updateStats.put("consecutiveDays", consecutiveDays);
                updateStats.put("lastStudyDate", today.toString());
                
                mapper.updateUserStats(updateStats);
                System.out.println("更新统计记录成功");
            }

            session.commit();
            return "Stats updated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) session.rollback();
            System.out.println("更新统计发生异常: " + e.getMessage());
            return "Error: " + e.getMessage();
        } finally {
            try {
                if (in != null) in.close();
                if (session != null) session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 🔧 新增方法：获取用户统计
    @GetMapping("/stats/{UID}")
    public Map<String, Object> getUserStats(@PathVariable int UID) {
        SqlSession session = null;
        InputStream in = null;
        try {
            System.out.println("查询用户统计: " + UID);

            in = Resources.getResourceAsStream("SqlMapConfig.xml");
            session = getSession(in);
            compRecOpMapper mapper = session.getMapper(compRecOpMapper.class);

            Map<String, Object> stats = mapper.getUserStats(UID);
            if (stats == null) {
                // 返回默认统计
                stats = new HashMap<>();
                stats.put("UID", UID);
                stats.put("total_questions", 0);
                stats.put("correct_questions", 0);
                stats.put("accuracy_rate", 0.0);
                stats.put("consecutive_days", 0);
                stats.put("last_study_date", null);
                System.out.println("用户无统计记录，返回默认值");
            } else {
                // 🔧 修复：确保日期字段返回正确的格式
                Object lastStudyDateObj = stats.get("last_study_date");
                if (lastStudyDateObj instanceof java.sql.Date) {
                    // 将 java.sql.Date 转换为 String
                    stats.put("last_study_date", ((java.sql.Date) lastStudyDateObj).toLocalDate().toString());
                }
                System.out.println("查询到用户统计: " + stats);
            }
            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("查询统计发生异常: " + e.getMessage());
            throw new RuntimeException("Error: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (session != null) session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

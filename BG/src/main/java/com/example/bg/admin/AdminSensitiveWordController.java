package com.example.bg.admin;

import com.example.bg.ConnetMySQL;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/sensitive-word")
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:5173",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:5173"
}, allowCredentials = "true")
public class AdminSensitiveWordController extends ConnetMySQL {

    @Autowired
    private ContentReviewService contentReviewService;

    /**
     * 获取所有敏感词
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有敏感词")
    public Map<String, Object> getAllSensitiveWords() {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            SensitiveWordMapper mapper = session.getMapper(SensitiveWordMapper.class);
            List<SensitiveWord> words = mapper.getAllSensitiveWords();

            result.put("success", true);
            result.put("data", words);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取敏感词列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 添加敏感词
     */
    @PostMapping("/add")
    @Operation(summary = "添加敏感词")
    public Map<String, Object> addSensitiveWord(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        String word = request.get("word");
        if (word == null || word.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "敏感词不能为空");
            return result;
        }

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            SensitiveWordMapper mapper = session.getMapper(SensitiveWordMapper.class);

            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word.trim());
            sensitiveWord.setActive(true);

            mapper.insertSensitiveWord(sensitiveWord);
            session.commit();

            // 清除缓存
            contentReviewService.clearCache();

            result.put("success", true);
            result.put("message", "敏感词添加成功");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加敏感词失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除敏感词")
    public Map<String, Object> deleteSensitiveWord(@PathVariable int id) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            SensitiveWordMapper mapper = session.getMapper(SensitiveWordMapper.class);
            mapper.deleteSensitiveWord(id);
            session.commit();

            // 清除缓存
            contentReviewService.clearCache();

            result.put("success", true);
            result.put("message", "敏感词删除成功");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除敏感词失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 启用/禁用敏感词
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用敏感词")
    public Map<String, Object> updateSensitiveWordStatus(
            @PathVariable int id,
            @RequestBody Map<String, Boolean> request) {

        Map<String, Object> result = new HashMap<>();
        Boolean isActive = request.get("isActive");

        if (isActive == null) {
            result.put("success", false);
            result.put("message", "状态参数不能为空");
            return result;
        }

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            SensitiveWordMapper mapper = session.getMapper(SensitiveWordMapper.class);
            mapper.updateSensitiveWordStatus(id, isActive);
            session.commit();

            // 清除缓存
            contentReviewService.clearCache();

            result.put("success", true);
            result.put("message", "敏感词状态更新成功");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新敏感词状态失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 刷新敏感词缓存
     */
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新敏感词缓存")
    public Map<String, Object> refreshCache() {
        Map<String, Object> result = new HashMap<>();

        try {
            contentReviewService.clearCache();
            result.put("success", true);
            result.put("message", "敏感词缓存已刷新");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "刷新缓存失败: " + e.getMessage());
        }

        return result;
    }
}
package com.example.bg.admin;

import com.example.bg.Comment.Comment;
import com.example.bg.Comment.CommentOpMapper;
import com.example.bg.ConnetMySQL;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/comment")
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://127.0.0.1:8081",  // 显式添加此项
        "http://117.72.88.23:8081",
},
        allowCredentials = "true")
public class AdminCommentController extends ConnetMySQL {

    /**
     * 获取所有评论列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取评论列表，支持分页和搜索")
    public Map<String, Object> getCommentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean hasTitle) {

        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);

            // 计算偏移量
            int offset = (page - 1) * size;

            // 获取评论列表
            List<Comment> comments = mapper.getCommentList(offset, size, search, category, hasTitle);

            // 获取总数
            int total = mapper.getCommentCount(search, category, hasTitle);

            result.put("success", true);
            result.put("data", comments);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取评论列表失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取所有主题帖（hasTitle=1的评论）
     */
    @GetMapping("/topics")
    @Operation(summary = "获取所有主题帖")
    public Map<String, Object> getTopicList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);

            int offset = (page - 1) * size;

            // 只获取主题帖
            List<Comment> topics = mapper.getTopicList(offset, size, search, category);
            int total = mapper.getTopicCount(search, category);

            result.put("success", true);
            result.put("data", topics);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取主题帖失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取评论详情及其回复
     */
    @GetMapping("/{commentId}")
    @Operation(summary = "获取评论详情及其所有回复")
    public Map<String, Object> getCommentDetail(@PathVariable int commentId) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);
            CommentOpMapper commentOpMapper = session.getMapper(CommentOpMapper.class);

            // 获取主评论
            Comment mainComment = mapper.getCommentById(commentId);
            if (mainComment == null) {
                result.put("success", false);
                result.put("message", "评论不存在");
                return result;
            }

            // 获取所有子评论
            List<Integer> allChildIds = getAllChild(commentId, commentOpMapper);
            List<Comment> allComments = commentOpMapper.getComment(allChildIds);

            result.put("success", true);
            result.put("mainComment", mainComment);
            result.put("allComments", allComments);
            result.put("totalReplies", allComments.size() - 1); // 减去主评论

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取评论详情失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 删除评论（包括所有子评论）
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论及其所有回复")
    public Map<String, Object> deleteComment(@PathVariable int commentId) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);
            CommentOpMapper commentOpMapper = session.getMapper(CommentOpMapper.class);

            // 验证评论是否存在
            Comment comment = mapper.getCommentById(commentId);
            if (comment == null) {
                result.put("success", false);
                result.put("message", "评论不存在");
                return result;
            }

            // 获取所有子评论ID
            List<Integer> allChildIds = getAllChild(commentId, commentOpMapper);

            // 删除评论及其关联数据
            commentOpMapper.delComment(allChildIds);
            commentOpMapper.delCommentAbout(allChildIds);

            session.commit();

            result.put("success", true);
            result.put("message", "评论删除成功，共删除 " + allChildIds.size() + " 条评论");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除评论失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 批量删除评论
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除评论")
    public Map<String, Object> batchDeleteComments(@RequestBody List<Integer> commentIds) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            CommentOpMapper commentOpMapper = session.getMapper(CommentOpMapper.class);

            int totalDeleted = 0;

            // 为每个评论获取所有子评论并删除
            for (Integer commentId : commentIds) {
                List<Integer> allChildIds = getAllChild(commentId, commentOpMapper);
                if (!allChildIds.isEmpty()) {
                    commentOpMapper.delComment(allChildIds);
                    commentOpMapper.delCommentAbout(allChildIds);
                    totalDeleted += allChildIds.size();
                }
            }

            session.commit();

            result.put("success", true);
            result.put("message", "批量删除成功，共删除 " + totalDeleted + " 条评论");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量删除失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取评论统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取评论统计数据")
    public Map<String, Object> getCommentStats() {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);

            // 获取各种统计数据
            int totalComments = mapper.getTotalCommentCount();
            int totalTopics = mapper.getTotalTopicCount();
            int todayComments = mapper.getTodayCommentCount();
            List<Map<String, Object>> categoryStats = mapper.getCategoryStats();
            List<Map<String, Object>> dailyStats = mapper.getDailyCommentStats(7); // 最近7天

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalComments", totalComments);
            stats.put("totalTopics", totalTopics);
            stats.put("todayComments", todayComments);
            stats.put("totalReplies", totalComments - totalTopics);
            stats.put("categoryStats", categoryStats);
            stats.put("dailyStats", dailyStats);

            result.put("success", true);
            result.put("data", stats);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 搜索评论
     */
    @GetMapping("/search")
    @Operation(summary = "搜索评论")
    public Map<String, Object> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);

            int offset = (page - 1) * size;

            List<Comment> comments = mapper.searchComments(keyword, offset, size);
            int total = mapper.getSearchCount(keyword);

            result.put("success", true);
            result.put("data", comments);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "搜索失败：" + e.getMessage());
        }

        return result;
    }

    // 辅助方法：获取所有子评论
    private List<Integer> getAllChild(int commentId, CommentOpMapper commentOpMapper) {
        List<Integer> result = new java.util.ArrayList<>();
        result.add(commentId);
        result.addAll(commentOpMapper.getChild(commentId));

        for (int i = 1; i < result.size(); i++) {
            result.addAll(commentOpMapper.getChild(result.get(i)));
        }

        return result;
    }
}
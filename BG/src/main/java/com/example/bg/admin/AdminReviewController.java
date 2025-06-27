package com.example.bg.admin;

import com.example.bg.Comment.Comment;
import com.example.bg.Comment.CommentOpMapper;
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
@RequestMapping("/admin/review")
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:5173",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:5173"
}, allowCredentials = "true")
public class AdminReviewController extends ConnetMySQL {

    @Autowired
    private ContentReviewService contentReviewService;

    /**
     * 扫描所有评论检测敏感词
     */
    @PostMapping("/scan")
    @Operation(summary = "扫描所有评论检测敏感词")
    public Map<String, Object> scanComments() {
        Map<String, Object> result = new HashMap<>();

        try {
            ContentReviewService.ScanResult scanResult = contentReviewService.scanAllComments();

            if (scanResult.success) {
                Map<String, Object> data = new HashMap<>();
                data.put("total", scanResult.totalComments);
                data.put("dangerous", scanResult.sensitiveComments);
                data.put("normal", scanResult.totalComments - scanResult.sensitiveComments);

                result.put("success", true);
                result.put("data", data);
                result.put("message", "扫描完成");
            } else {
                result.put("success", false);
                result.put("message", "扫描失败: " + scanResult.errorMessage);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "扫描失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取包含敏感词的评论列表
     */
    @GetMapping("/sensitive")
    @Operation(summary = "获取包含敏感词的评论列表")
    public Map<String, Object> getSensitiveComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);

            int offset = (page - 1) * size;
            List<Comment> sensitiveComments = mapper.getSensitiveComments(offset, size);
            int total = mapper.getSensitiveCommentCount();

            result.put("success", true);
            result.put("data", sensitiveComments);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取敏感评论失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 将评论标记为正常（取消敏感词标记）
     */
    @PutMapping("/comment/{commentId}/mark-safe")
    @Operation(summary = "将评论标记为正常")
    public Map<String, Object> markCommentSafe(@PathVariable int commentId) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);

            // 标记为正常状态
            mapper.updateCommentReviewStatus(commentId, 1);
            session.commit();

            result.put("success", true);
            result.put("message", "评论已标记为正常");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "标记失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 删除单个敏感评论
     */
    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "删除敏感评论")
    public Map<String, Object> deleteSensitiveComment(@PathVariable int commentId) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            CommentOpMapper commentOpMapper = session.getMapper(CommentOpMapper.class);

            // 删除评论及其子评论
            List<Integer> toDelete = List.of(commentId);
            commentOpMapper.delComment(toDelete);

            // 删除子评论
            List<Integer> childIds = commentOpMapper.getChild(commentId);
            if (!childIds.isEmpty()) {
                commentOpMapper.delComment(childIds);
            }

            session.commit();

            result.put("success", true);
            result.put("message", "敏感评论删除成功");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 批量删除所有敏感评论
     */
    @DeleteMapping("/sensitive/batch")
    @Operation(summary = "批量删除所有敏感评论")
    public Map<String, Object> batchDeleteSensitiveComments() {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper adminMapper = session.getMapper(AdminCommentMapper.class);
            CommentOpMapper commentOpMapper = session.getMapper(CommentOpMapper.class);

            // 获取所有敏感评论ID
            List<Integer> sensitiveIds = adminMapper.getSensitiveCommentIds();

            if (!sensitiveIds.isEmpty()) {
                // 批量删除
                commentOpMapper.delComment(sensitiveIds);
                session.commit();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("deletedCount", sensitiveIds.size());

            result.put("success", true);
            result.put("data", data);
            result.put("message", "批量删除成功，共删除 " + sensitiveIds.size() + " 条敏感评论");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量删除失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取审核统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取审核统计信息")
    public Map<String, Object> getReviewStats() {
        Map<String, Object> result = new HashMap<>();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper mapper = session.getMapper(AdminCommentMapper.class);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalComments", mapper.getTotalCommentCount());
            stats.put("sensitiveComments", mapper.getSensitiveCommentCount());
            stats.put("normalComments", mapper.getTotalCommentCount() - mapper.getSensitiveCommentCount());

            result.put("success", true);
            result.put("data", stats);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }

        return result;
    }
}
package com.example.bg.admin;

import com.example.bg.admin.SensitiveWord;
import com.example.bg.admin.SensitiveWordMapper;
import com.example.bg.admin.AdminCommentMapper;
import com.example.bg.Comment.Comment;
import com.example.bg.ConnetMySQL;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ContentReviewService extends ConnetMySQL {

    // 敏感词缓存
    private volatile List<SensitiveWord> sensitiveWordsCache = null;
    private volatile long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存

    // 定时刷新缓存的线程池
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ContentReviewService() {
        // 启动定时任务，每一天刷新一次缓存
        scheduler.scheduleAtFixedRate(this::refreshSensitiveWordsCache, 0, 1, TimeUnit.DAYS);
    }

    /**
     * 检测文本是否包含敏感词
     * @param text 要检测的文本
     * @return true-包含敏感词, false-不包含
     */
    public boolean containsSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        List<SensitiveWord> sensitiveWords = getSensitiveWords();
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        for (SensitiveWord sensitiveWord : sensitiveWords) {
            if (sensitiveWord.getWord() != null &&
                    lowerText.contains(sensitiveWord.getWord().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 扫描所有评论，检测敏感词并更新状态
     * @return 扫描结果统计
     */
    public ScanResult scanAllComments() {
        ScanResult result = new ScanResult();

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            AdminCommentMapper commentMapper = session.getMapper(AdminCommentMapper.class);

            // 获取所有评论
            List<Comment> allComments = commentMapper.getAllComments();
            result.totalComments = allComments.size();

            int sensitiveCount = 0;

            // 检测每条评论
            for (Comment comment : allComments) {
                boolean hasSensitiveContent = false;

                // 检测标题
                if (comment.getTitle() != null && containsSensitiveWords(comment.getTitle())) {
                    hasSensitiveContent = true;
                }

                // 检测内容
                if (!hasSensitiveContent && comment.getContent() != null &&
                        containsSensitiveWords(comment.getContent())) {
                    hasSensitiveContent = true;
                }

                // 更新审核状态
                int newStatus = hasSensitiveContent ? 2 : 1; // 2-包含敏感词, 1-正常
                commentMapper.updateCommentReviewStatus(comment.getCommentID(), newStatus);

                if (hasSensitiveContent) {
                    sensitiveCount++;
                }
            }

            session.commit();
            result.sensitiveComments = sensitiveCount;
            result.success = true;

        } catch (Exception e) {
            e.printStackTrace();
            result.success = false;
            result.errorMessage = e.getMessage();
        }

        return result;
    }

    /**
     * 获取敏感词列表（带缓存）
     */
    private List<SensitiveWord> getSensitiveWords() {
        long currentTime = System.currentTimeMillis();

        // 检查缓存是否过期
        if (sensitiveWordsCache == null || (currentTime - lastCacheTime) > CACHE_DURATION) {
            refreshSensitiveWordsCache();
        }

        return sensitiveWordsCache;
    }

    /**
     * 刷新敏感词缓存
     */
    private void refreshSensitiveWordsCache() {
        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {

            SensitiveWordMapper mapper = session.getMapper(SensitiveWordMapper.class);
            sensitiveWordsCache = mapper.getActiveSensitiveWords();
            lastCacheTime = System.currentTimeMillis();

            System.out.println("敏感词缓存已刷新，共加载 " +
                    (sensitiveWordsCache != null ? sensitiveWordsCache.size() : 0) + " 个敏感词");

        } catch (Exception e) {
            System.err.println("刷新敏感词缓存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 手动清除缓存
     */
    public void clearCache() {
        sensitiveWordsCache = null;
        lastCacheTime = 0;
        refreshSensitiveWordsCache();
    }

    /**
     * 扫描结果类
     */
    public static class ScanResult {
        public boolean success = false;
        public int totalComments = 0;
        public int sensitiveComments = 0;
        public String errorMessage = null;
    }
}
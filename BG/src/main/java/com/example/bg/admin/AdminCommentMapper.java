package com.example.bg.admin;

import com.example.bg.Comment.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface AdminCommentMapper {

    // 获取评论列表（分页）
    List<Comment> getCommentList(@Param("offset") int offset,
                                 @Param("size") int size,
                                 @Param("search") String search,
                                 @Param("category") String category,
                                 @Param("hasTitle") Boolean hasTitle);

    // 获取评论总数
    int getCommentCount(@Param("search") String search,
                        @Param("category") String category,
                        @Param("hasTitle") Boolean hasTitle);

    // 获取主题帖列表
    List<Comment> getTopicList(@Param("offset") int offset,
                               @Param("size") int size,
                               @Param("search") String search,
                               @Param("category") String category);

    // 获取主题帖总数
    int getTopicCount(@Param("search") String search, @Param("category") String category);

    // 根据ID获取评论
    Comment getCommentById(@Param("commentId") int commentId);

    // 搜索评论
    List<Comment> searchComments(@Param("keyword") String keyword,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    // 获取搜索结果总数
    int getSearchCount(@Param("keyword") String keyword);

    // 统计相关
    int getTotalCommentCount();
    int getTotalTopicCount();
    int getTodayCommentCount();
    List<Map<String, Object>> getCategoryStats();
    List<Map<String, Object>> getDailyCommentStats(@Param("days") int days);

    // ======== 新增：审核相关方法 ========

    // 获取所有评论（用于扫描）
    List<Comment> getAllComments();

    // 更新评论审核状态
    void updateCommentReviewStatus(@Param("commentId") int commentId, @Param("reviewStatus") int reviewStatus);

    // 获取包含敏感词的评论
    List<Comment> getSensitiveComments(@Param("offset") int offset, @Param("size") int size);

    // 获取包含敏感词的评论总数
    int getSensitiveCommentCount();

    // 批量获取包含敏感词的评论ID
    List<Integer> getSensitiveCommentIds();
}
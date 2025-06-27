package com.example.bg.Comment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Comment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public int CommentID, LikeCounts, CommentCounts, PersonID, parentID;
    public String Timestamp, Category, Title, Content;
    public boolean hasTitle, isAdmin;

    // 新增审核状态字段
    public int reviewStatus = 0; // 0-未审核, 1-正常, 2-包含敏感词

    // Getter和Setter方法
    public int getCommentID() { return CommentID; }
    public void setCommentID(int commentID) { CommentID = commentID; }

    public int getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(int reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getContent() { return Content; }
    public void setContent(String content) { Content = content; }

    public String getTitle() { return Title; }
    public void setTitle(String title) { Title = title; }
}
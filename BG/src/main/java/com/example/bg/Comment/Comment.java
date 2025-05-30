package com.example.bg.Comment;

import lombok.Data;

@Data
public class Comment {
    private int CommentID,nextCID,LikeCounts,CommentCounts,PersonID;
    private String Timestamp,Category,Title,Content;
    private boolean hasTitle;
}

package com.example.bg.Comment;

import lombok.Data;

@Data
public class Comment {
    public int CommentID,LikeCounts,CommentCounts,PersonID,parentID;
    public String Timestamp,Category,Title,Content;
    public boolean hasTitle,isAdmin;
}

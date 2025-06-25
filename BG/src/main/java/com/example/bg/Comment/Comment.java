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
}
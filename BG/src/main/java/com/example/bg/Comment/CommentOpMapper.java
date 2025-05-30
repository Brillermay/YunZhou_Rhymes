package com.example.bg.Comment;

import javax.swing.text.AbstractDocument;
import java.util.List;

//基础逻辑：
//初始化时调用getComment(Init())，渲染所有贴子
//然后查找时调用getComment(OpenComment(id))，比如传入某一个的回帖id


public interface CommentOpMapper {
    List<Comment> getComment(List<Integer> CIDs);//传入CIDs返回Comment类的列表
    void delComment(int CID);
    void addComment(int CID);
    List<Integer>Init();
    List<Integer>OpenComment();
}

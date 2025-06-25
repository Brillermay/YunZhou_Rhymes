package com.example.bg.Comment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

//基础逻辑：
//初始化时调用getComment(Init())，渲染所有贴子
//然后查找时调用getComment(OpenComment(id))，比如传入某一个的回帖id

@Mapper
@Repository
public interface CommentOpMapper {
    List<Comment> getComment(List<Integer> CIDs);//传入CIDs返回Comment类的列表
    void delComment(List<Integer> CIDs);
    List<Integer>Init();
    void insertComment(Comment comment);//通过request提交表单
    List<Integer>getChild(int CID);//返回某一个节点对应的子节点
    void incrementCommentCount(@Param("commentId") int commentId);
    void delCommentAbout(List<Integer>CIDs);
    void updLinklist(int CID,int NCID);

    /*评论点赞相关*/
    void updLikeList(int CID,int UID);//传入UID、CID，完成：联表数据插入
    void updLikeNum(int CID);// 完成：点赞数据自增
    void delLikeList(int CID,int UID);
    void delLikeNum(int CID);
    List<Integer>getLikeList(int UID);//传入UID,返回喜欢的评论CID
}

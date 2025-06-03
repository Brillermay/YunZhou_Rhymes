package com.example.bg.Comment;

import com.example.bg.ConnetMySQL;
import com.example.bg.ID.IDsGetMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryEventPublisherBeansTestExecutionListener;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(
        origins = {
                "http://localhost:8080",
                "http://localhost:8081",
                "http://127.0.0.1:8081",  // 显式添加此项
                "http://117.72.88.23:8081",
        },
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, // 添加 OPTIONS
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@RequestMapping(value = "/comment")
public class CommentOp extends ConnetMySQL {
    @GetMapping("/init")
    @Operation(summary = "初始化，返回所有评论树的根节点对象")
    public List<Comment>Init()
    {
        List<Comment>ans;
        try {
            List<Integer>tmp;
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            CommentOpMapper commentOpMapper=session.getMapper(CommentOpMapper.class);
            tmp=commentOpMapper.Init();
            ans=commentOpMapper.getComment(tmp);
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    public List<Integer> getAllChild(int CID){
        try {
            List<Integer>tmp=new ArrayList<>();
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            CommentOpMapper commentOpMapper=session.getMapper(CommentOpMapper.class);
            tmp.add(CID);
            tmp.addAll(commentOpMapper.getChild(CID));
            for(int i=1;i<tmp.size();i++)
            {
                tmp.addAll(commentOpMapper.getChild(tmp.get(i)));
            }
            in.close();
            session.close();
            return tmp;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    @GetMapping("/del/{cid}")
    @Operation(summary = "bfs式删除评论树")
    public void delCommentTree(@PathVariable int cid) throws IOException{

        try {
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            CommentOpMapper commentOpMapper=session.getMapper(CommentOpMapper.class);
            List<Integer>tmp=getAllChild(cid);
            commentOpMapper.delComment(tmp);
            in.close();
            session.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    @GetMapping("/open_comment/{cid}")
    @Operation(summary = "返回这个评论树id")
    public List<Comment> getCommentTree(@PathVariable int cid)throws IOException{
        try {
            List<Comment>ans;
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            CommentOpMapper commentOpMapper=session.getMapper(CommentOpMapper.class);
            ans=commentOpMapper.getComment(getAllChild(cid));
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    /*
    * IDsGetMapper iDsGetMapper=session.getMapper(IDsGetMapper.class);
            int ans=iDsGetMapper.getCID();
            * */

}

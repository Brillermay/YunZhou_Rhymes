package com.example.bg.Comment;

import com.example.bg.ConnetMySQL;
import com.example.bg.ID.IDsGetMapper;
import com.example.bg.RedisService.RedisCommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/comment")
public class CommentController extends ConnetMySQL {
    @Autowired
    private RedisCommentService redisCommentService;
    @GetMapping("/init")
    @Operation(summary = "初始化，返回所有评论树的根节点对象")
    public List<Comment>Init()
    {
        List<Comment> cachedRoots = redisCommentService.getCachedRoots();
        if (cachedRoots != null) return cachedRoots;
        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {
            CommentOpMapper commentOpMapper = session.getMapper(CommentOpMapper.class);
            List<Integer> tmp = commentOpMapper.Init();
            List<Comment> ans = commentOpMapper.getComment(tmp);

            redisCommentService.cacheRootComments(ans);
            return ans;
        } catch (Exception e) {
            throw new RuntimeException("error:" + e.getMessage());
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
            commentOpMapper.delCommentAbout(tmp);
            session.commit();
            redisCommentService.evictTreeCache(cid);
            redisCommentService.evictRootCache();
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
        List<Comment> cachedTree = redisCommentService.getCachedTree(cid);
        if (cachedTree != null) return cachedTree.subList(1,  cachedTree.size());

        try (InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
             SqlSession session = getSession(in)) {
            CommentOpMapper commentOpMapper = session.getMapper(CommentOpMapper.class);
            List<Comment> ans = commentOpMapper.getComment(getAllChild(cid));

            redisCommentService.cacheCommentTree(cid,  ans);
            return ans.subList(1,  ans.size());
        } catch (Exception e) {
            throw new RuntimeException("error:" + e.getMessage());
        }
    }
    @PostMapping("/addComment")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "添加新评论")
    public ResponseEntity<?> addComment(
            @Valid @RequestBody Comment comment,
            BindingResult result
    ) {
        // ===== 1. 表单验证 =====
        if (result.hasErrors())  {
            List<String> errors = result.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField()  + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "VALIDATION_FAILED",
                            "errors", errors,
                            "timestamp", LocalDateTime.now().toString()
                    ));
        }

        SqlSession session = null;
        InputStream in = null;

        try {
            // ===== 2. 初始化数据库连接 =====
            in = Resources.getResourceAsStream("SqlMapConfig.xml");
            session = getSession(in);
            CommentOpMapper mapper = session.getMapper(CommentOpMapper.class);
            IDsGetMapper iDsGetMapper=session.getMapper(IDsGetMapper.class);
            // ===== 3. 数据预处理 =====
            // 设置服务器生成属性
            comment.Timestamp=LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            comment.LikeCounts=0;
            comment.CommentCounts=0;

            comment.CommentID=iDsGetMapper.getCID();
            iDsGetMapper.updateCID();
            // 敏感词过滤
            comment.Content=filterSensitiveWords(comment.Content);

            // ===== 4. 数据库操作 =====
            // 4.1 插入主评论
            mapper.insertComment(comment);
            mapper.updLinklist(comment.CommentID,comment.parentID);
            // 4.2 更新父评论（如果是回复）
            while (comment.parentID  > 0) {
                mapper.incrementCommentCount(comment.parentID);
                comment=mapper.getComment(Collections.singletonList(comment.parentID)).get(0);
            }

            // 4.3 提交事务
            session.commit();
            if (comment.parentID  == 0) {
                redisCommentService.evictRootCache();
            } else {
                redisCommentService.evictTreeCache(comment.parentID);
            }
            session.close();
            in.close();
            // ===== 5. 构建响应 =====
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", "SUCCESS",
                            "message", "评论添加成功",
                            "commentId", comment.CommentID,
                            "createdAt", comment.Timestamp
                    ));

        } catch (Exception e) {
            // ===== 6. 异常处理 =====
            e.printStackTrace();

            // 回滚事务
            if (session != null) {
                session.rollback();
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "SERVER_ERROR",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now().toString()
                    ));

        } finally {
            try {
                if (in != null) in.close();
                if (session != null) session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/delLikeComment/{uid}/{cid}")
    @Operation(summary = "删去喜欢的comment")
    public void delLikeCounts(@PathVariable int uid,@PathVariable int cid)throws IOException{
        try {
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            CommentOpMapper commentOpMapper=session.getMapper(CommentOpMapper.class);
            commentOpMapper.delLikeList(cid,uid);
            commentOpMapper.delLikeNum(cid);
            session.commit();

            in.close();
            session.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }

    @GetMapping("/likeComment/{uid}/{cid}")
    @Operation(summary = "给评论添加喜好")
    public void addLikeCounts(@PathVariable int uid,@PathVariable int cid)throws IOException{
        try {
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            CommentOpMapper commentOpMapper=session.getMapper(CommentOpMapper.class);
            commentOpMapper.updLikeList(cid,uid);
            commentOpMapper.updLikeNum(cid);
            session.commit();
            in.close();
            session.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    @GetMapping("/getLikeIDs/{uid}")
    @Operation(summary = "返回当前uid对应的喜欢的评论list")
    public List<Integer>retLikeCIDs(@PathVariable int uid)throws IOException
    {
        try {
            List<Integer>ans;
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            CommentOpMapper commentOpMapper=session.getMapper(CommentOpMapper.class);
            ans=commentOpMapper.getLikeList(uid);

            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }

    // 敏感词过滤方法
    private String filterSensitiveWords(String content) {
        // 实际项目中应从数据库或配置文件加载
        List<String> sensitiveWords = Arrays.asList(" 敏感词1", "敏感词2", "不良内容");

        for (String word : sensitiveWords) {
            content = content.replaceAll(word,  "***");
        }
        return content;
    }

}

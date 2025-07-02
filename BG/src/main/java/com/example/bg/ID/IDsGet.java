package com.example.bg.ID;

import com.example.bg.Comment.Comment;
import com.example.bg.ConnetMySQL;
import com.example.bg.poem.Poem;
import com.example.bg.poem.PoemGetMapper;
import com.example.bg.poem.Poet;
import com.example.bg.poem.PoetGetMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/id")
public class IDsGet extends ConnetMySQL  {
    @GetMapping("/get_cid")
    @Operation(summary = "不需要传递参数，返回一个整数作为评论的cid")
    public Integer getCID() throws IOException
    {
        try {
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            IDsGetMapper iDsGetMapper=session.getMapper(IDsGetMapper.class);
            Integer ans=iDsGetMapper.getCID();
            iDsGetMapper.updateCID();
            session.commit();
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    @GetMapping("/add_star_id/{UID}/{PID}")
    @Operation(summary = "传入/uid/pid，添加到收藏列表")
    public void addStarID(@PathVariable int UID,@PathVariable int PID) throws IOException
    {
        try {

            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            IDsGetMapper iDsGetMapper=session.getMapper(IDsGetMapper.class);
            iDsGetMapper.addStarID(PID,UID);
            session.commit();
            in.close();
            session.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    @GetMapping("/del_star_id/{UID}/{PID}")
    @Operation(summary = "传入/uid/pid，从收藏列表删除")
    public void delStarID(@PathVariable int UID,@PathVariable int PID) throws IOException
    {
        try {

            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            IDsGetMapper iDsGetMapper=session.getMapper(IDsGetMapper.class);
            iDsGetMapper.delStarID(PID,UID);
            session.commit();
            in.close();
            session.close();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }

}

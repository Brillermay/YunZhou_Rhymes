package com.example.bg.poem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.example.bg.RedisService.RedisCommentService;
import com.example.bg.RedisService.RedisTreeService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.bg.ConnetMySQL;

import io.swagger.v3.oas.annotations.Operation;



@RestController
@RequestMapping(value = "/poem")
public class PoemGet extends ConnetMySQL {
    @Autowired
    private RedisTreeService redisTreeService;
    @GetMapping("/{id}")
    @Operation(summary = "传递参数为诗词id，返回符合条件的诗词")
    public Poem getPoem(@PathVariable String id)throws IOException{
        //接下来使用工厂方法进行查询功能的实现

        try {
            int idInt=Integer.parseInt(id);
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            PoemGetMapper getClimateMapper=session.getMapper(PoemGetMapper.class);
            Poem ans=getClimateMapper.getPoem(idInt);
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
    @GetMapping("/keyword/{ky}")
    @Operation(summary = "传递参数为关键字，返回包含该关键字的诗词列表")
    public List<Poem> getPoems(@PathVariable String ky)throws IOException{
        //接下来使用工厂方法进行查询功能的实现
        List<Poem>cachedPoems=redisTreeService.getCachedTree(ky);
        //System.out.println("内存查找中！");
        if(cachedPoems!=null)return cachedPoems;
        //System.out.println("内存未找到！磁盘寻找中！");
        try {
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            PoemGetMapper poemGetMapper=session.getMapper(PoemGetMapper.class);
            List<Poem> ans=poemGetMapper.getPoems(ky);
            redisTreeService.cacheCommentTree(ky,ans);
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }


}

package com.example.bg.poem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.bg.ConnetMySQL;

import io.swagger.v3.oas.annotations.Operation;


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
@RequestMapping(value = "/poem")
public class PoemGet extends ConnetMySQL {
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
        try {
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            PoemGetMapper poemGetMapper=session.getMapper(PoemGetMapper.class);
            List<Poem> ans=poemGetMapper.getPoems(ky);
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }


}

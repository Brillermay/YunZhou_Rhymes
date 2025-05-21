package com.example.bg.problem;

import com.example.bg.ConnetMySQL;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.io.InputStream;
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
@RequestMapping(value = "/problem")
public class ProblemGet extends ConnetMySQL {
    @GetMapping("/{id}")
    @Operation(summary = "传递参数为问题id，返回符合条件的问题")
    public Problem getProblem(@PathVariable String id)throws IOException{
        //接下来使用工厂方法进行查询功能的实现
        try {
            int idInt=Integer.parseInt(id);
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            ProblemGetMapper getClimateMapper=session.getMapper(ProblemGetMapper.class);
            Problem ans=getClimateMapper.getProblem(idInt);
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
}

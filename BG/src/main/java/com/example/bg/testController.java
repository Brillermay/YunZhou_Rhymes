package com.example.bg;

import com.example.bg.poem.Poem;
import com.example.bg.poem.PoemGetMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

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
@RequestMapping(value = "/con")
public class testController extends ConnetMySQL{
    @GetMapping("/")
    @Operation(summary = "传递参数为诗词id，返回符合条件的诗词")
    public int getPoem()throws IOException {
        //接下来使用工厂方法进行查询功能的实现
        try {
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            testMapper testMapper=session.getMapper(testMapper.class);
            testMapper.retT();
            session.commit();
            in.close();
            session.close();
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }

}

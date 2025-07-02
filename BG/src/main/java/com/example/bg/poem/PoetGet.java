package com.example.bg.poem;

import com.example.bg.ConnetMySQL;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;



@RestController
@RequestMapping(value = "/poet")
public class PoetGet extends ConnetMySQL {
    //这里我不知道诗人是传什么参数，所以就先不改
    @GetMapping("/{id}")
    @Operation(summary = "传递参数为诗人id，返回符合条件的诗人")
    public Poet getPoet(@PathVariable String id)throws IOException{
        //接下来使用工厂方法进行查询功能的实现
        try {
            int idInt=Integer.parseInt(id);
            InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");//这里都一样的
            SqlSession session=getSession(in);
            PoetGetMapper getClimateMapper=session.getMapper(PoetGetMapper.class);
            Poet ans=getClimateMapper.getPoet(idInt);
            in.close();
            session.close();
            return ans;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("error:"+e.getMessage());
        }
    }
}

package com.example.bg.user;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
                "http://127.0.0.1:8081",
                "http://117.72.88.23:8081",
        },
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@RequestMapping(value = "/user")
public class UserDel extends ConnetMySQL {

    @DeleteMapping("/del/{uid}")
    @Operation(summary = "根据UID删除用户")
    public String delUser(@PathVariable int uid) throws IOException {
        try {
            InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
            SqlSession session = getSession(in);
            UserDelMapper userDelMapper = session.getMapper(UserDelMapper.class);
            int result = userDelMapper.delUser(uid);
            session.commit();
            in.close();
            session.close();
            return result > 0 ? "删除成功" : "删除失败";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error:" + e.getMessage());
        }
    }
}
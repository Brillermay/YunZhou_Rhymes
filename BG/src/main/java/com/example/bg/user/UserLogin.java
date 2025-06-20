package com.example.bg.user;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.*;

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
public class UserLogin extends ConnetMySQL {

    @PostMapping("/login")
    @Operation(summary = "用户登录，传递User对象，返回登录成功与否")
    public String login(@RequestBody User user) throws IOException {
        try {
            InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
            SqlSession session = getSession(in);
            UserLoginMapper userLoginMapper = session.getMapper(UserLoginMapper.class);
            User result = userLoginMapper.login(user);
            in.close();
            session.close();
            return result != null ? "登录成功" : "登录失败";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error:" + e.getMessage());
        }
    }

    @GetMapping("/loginID/{username}")
    @Operation(summary = "返回id")
    public int retRegisID(@PathVariable String username)throws IOException
    {
        try {
            InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
            SqlSession session = getSession(in);
            UserLoginMapper userLoginMapper = session.getMapper(UserLoginMapper.class);
            int result = userLoginMapper.getID(username);
            in.close();
            session.close();
            return result ;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error:" + e.getMessage());
        }
    }

    @GetMapping("/loginName/{uid}")
    @Operation(summary = "返回名字")
    public String retName(@PathVariable int uid)throws IOException
    {
        try {
            InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
            SqlSession session = getSession(in);
            UserLoginMapper userLoginMapper = session.getMapper(UserLoginMapper.class);
            String result = userLoginMapper.getName(uid);
            in.close();
            session.close();
            return result ;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error:" + e.getMessage());
        }
    }

}
package com.example.bg.user;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(
        origins = {"http://localhost:8080", "http://localhost:8081",
                "http://127.0.0.1:8081", "http://117.72.88.23:8081"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS},
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@RequestMapping("/user")
public class UserAdd {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/add")
    @Operation(summary = "添加用户")
    public String addUser(@RequestBody Map<String, String> request) {
        User user=new User();
        user.setName(request.get("UserName"));
        user.setSalt(UUID.randomUUID().toString());
        user.setPwd(encryptPassword(request.get("PassWord"),  user.getSalt()));
        user.setStatus("active");

        User res=userMapper.findByUsername(user.getName());
        if(res != null)return "用户名重复！";

        int result = userMapper.UserAdd(user);
        return result > 0 ? "添加成功" : "添加失败";
    }

    private String encryptPassword(String password, String salt) {
        return new SimpleHash("SHA-256", password, ByteSource.Util.bytes(salt),  1024).toHex();
    }

    // 使用Shiro的认证机制
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public String login(@RequestBody Map<String, String> request) {
        Subject subject = SecurityUtils.getSubject();
        String username = request.get("UserName");  // 必须与JSON键名一致
        String password = request.get("PassWord");
        // 创建Token时必须传递username
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        System.out.println("Controller 层收到用户名：" + username);
        try {
            subject.login(token);
            return "登录成功";
        } catch (AuthenticationException e) {
            return "登录失败: " + e.getMessage();
        }
    }

    @DeleteMapping("/del/{uid}")
    @Operation(summary = "根据UID删除用户")
    public String delUser(@PathVariable int uid) {
        int result = userMapper.delUser(uid);
        return result > 0 ? "删除成功" : "删除失败";
    }

    @GetMapping("/loginID/{username}")
    @Operation(summary = "返回用户ID")
    public int retRegisID(@PathVariable String username) {
        return userMapper.getID(username);
    }

    @GetMapping("/loginName/{uid}")
    @Operation(summary = "返回用户名")
    public String retName(@PathVariable int uid) {
        return userMapper.getName(uid);
    }
}
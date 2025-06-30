package com.example.bg.user;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.HashMap;
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
public class UserController {

    @Autowired
    private UserService userService;// 在第 36 行左右，修改 addUser 方法
    @PostMapping("/add")
    @Operation(summary = "添加用户")
    public String addUser(@RequestBody Map<String, String> request) {
        User user = new User();
        user.setName(request.get("UserName"));
        user.setSalt(UUID.randomUUID().toString());
        user.setPwd(userService.encryptPassword(request.get("PassWord"), user.getSalt()));
        user.setStatus("active");
        user.setIsadmin(0);
        
        // 🔧 新增：处理新字段
        user.setNickname(request.get("Nickname")); // 前端传入昵称，如果为空则使用用户名
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getName()); // 默认昵称为用户名
        }
        user.setEmail(request.get("Email")); // 前端传入邮箱（可选）
        
        int result = userService.addUser(user);
        return result > 0 ? "添加成功" : "添加失败，请修改用户名";
    }



    // 使用Shiro的认证机制
    // 在第 46 行左右，修改 login 方法
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("UserName");
        String password = request.get("PassWord");
        
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        
        try {
            subject.login(token);
            
            // 🔧 新增：获取完整用户信息
            User user = userService.findByUsername(username);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("uid", user.getUID());
            response.put("username", user.getName());
            response.put("nickname", user.getNickname());
            response.put("email", user.getEmail());
            response.put("isAdmin", user.getIsadmin() == 1);
            
            return response;
        } catch (AuthenticationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "登录失败: " + e.getMessage());
            return errorResponse;
        }
    }

    @DeleteMapping("/del/{uid}")
    @Operation(summary = "根据UID删除用户")
    public String delUser(@PathVariable int uid) {
        return userService.delUser(uid) > 0 ? "删除成功" : "删除失败";
    }

    @GetMapping("/loginID/{username}")
    @Operation(summary = "返回用户ID")
    public int retRegisID(@PathVariable String username) {
        return userService.getUID(username);
    }

    @GetMapping("/loginName/{uid}")
    @Operation(summary = "返回用户名")
    public String retName(@PathVariable int uid) {
        return userService.getName(uid);
    }

    @GetMapping("/changePWD")
    @Operation(summary = "修改密码")
    public String changePwd(@RequestBody Map<String,String>request){
        User user = new User();
        String username = request.get("UserName");
        int uid=userService.getUID(username);
        userService.delUser(uid);
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString());
        user.setPwd(userService.encryptPassword(request.get("PassWord"),  user.getSalt()));
        user.setStatus("active");
        user.setIsadmin(0);
        int result = userService.addUser(user);
        return result>0?"修改成功":"修改失败";
    }
}
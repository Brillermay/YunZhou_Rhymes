package com.example.bg.user;

import io.swagger.v3.oas.annotations.Operation;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class GMController {
    @Autowired
    private UserService userService;
    
    @PostMapping("/add")
    @Operation(summary = "添加管理员")
    public String addGM(@RequestBody Map<String, String> request) {
        User user = new User();
        user.setName(request.get("UserName"));
        user.setSalt(UUID.randomUUID().toString());
        user.setPwd(userService.encryptPassword(request.get("PassWord"), user.getSalt()));
        user.setStatus("active");
        user.setIsadmin(1); // 管理员
        
        // 处理新字段
        user.setNickname(request.get("Nickname"));
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getName()); // 默认昵称为用户名
        }
        user.setEmail(request.get("Email"));
        
        int result = userService.addUser(user);
        return result > 0 ? "添加成功" : "添加失败，请修改用户名";
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("UserName");
        String password = request.get("PassWord");
        
        // 添加参数验证
        if (username == null || username.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户名不能为空");
            return errorResponse;
        }
        
        if (password == null || password.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "密码不能为空");
            return errorResponse;
        }
        
        Subject subject = SecurityUtils.getSubject();
        
        // 如果当前已有会话，先清理
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        
        try {
            subject.login(token);
            
            // 获取完整用户信息并验证是否为管理员
            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户不存在");
                return errorResponse;
            }
            
            if (user.getIsadmin() != 1) {
                subject.logout(); // 非管理员，登出
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "非管理员账户");
                return errorResponse;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("uid", user.getUID());
            response.put("username", user.getName());
            response.put("nickname", user.getNickname());
            response.put("isAdmin", true);
            
            return response;
        } catch (AuthenticationException e) {
            System.out.println("登录失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "登录失败: 用户名或密码错误");
            return errorResponse;
        } catch (Exception e) {
            System.out.println("登录异常: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "登录失败: 系统异常");
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
}
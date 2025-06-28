package com.example.bg.user;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/admin")
public class GMController {
    @Autowired
    private UserService userService;
    @PostMapping("/add")
    @Operation(summary = "添加管理员")
    public String addGM(@RequestBody Map<String, String> request) {
        User user=new User();
        user.setName(request.get("UserName"));
        user.setSalt(UUID.randomUUID().toString());
        user.setPwd(userService.encryptPassword(request.get("PassWord"),  user.getSalt()));
        user.setStatus("active");
        user.setIsadmin(1);
        int result = userService.addUser(user);
        return result > 0 ? "添加成功" : "添加失败，请修改用户名";
    }

    // 使用Shiro的认证机制
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public String login(@RequestBody Map<String, String> request) {
        String username = request.get("UserName");  // 必须与JSON键名一致
        String password = request.get("PassWord");
        return userService.login(username,password);

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

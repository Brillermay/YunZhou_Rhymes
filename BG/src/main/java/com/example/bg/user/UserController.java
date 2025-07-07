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

import java.util.*;


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



    // 使用Shiro的认证机制//
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
            response.put("avatar", user.getAvatar()); // 🔧 添加头像字段
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

// 替换原有的 changePWD 方法（大约第95行）

    @PostMapping("/changePWD")  // 🔧 改为POST方法
    @Operation(summary = "修改密码")
    public Map<String, Object> changePwd(@RequestBody Map<String,String> request){
        try {
            String username = request.get("UserName");
            String oldPassword = request.get("OldPassword");
            String newPassword = request.get("NewPassword");
            
            // 验证旧密码
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(username, oldPassword);
            
            try {
                subject.login(token);
            } catch (AuthenticationException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "旧密码错误");
                return errorResponse;
            }
            
            // 获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户不存在");
                return errorResponse;
            }
            
            // 🔧 只更新密码相关字段，保留其他信息
            user.setSalt(UUID.randomUUID().toString());
            user.setPwd(userService.encryptPassword(newPassword, user.getSalt()));
            
            int result = userService.updateUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result > 0);
            response.put("message", result > 0 ? "密码修改成功" : "密码修改失败");
            return response;
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "密码修改失败: " + e.getMessage());
            return errorResponse;
        }
    }

        @PostMapping("/updateAvatar")
    @Operation(summary = "更新用户头像")
    public Map<String, Object> updateAvatar(@RequestBody Map<String, String> request) {
        try {
            int uid = Integer.parseInt(request.get("uid"));
            String avatarBase64 = request.get("avatar");
            
            User user = userService.findByUID(uid);
            if (user != null) {
                user.setAvatar(avatarBase64);
                userService.updateUser(user);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "头像更新成功");
                response.put("avatar", avatarBase64);
                return response;
            }
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户不存在");
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "头像更新失败: " + e.getMessage());
            return errorResponse;
        }
    }

    @GetMapping("/profile/{uid}")
    @Operation(summary = "获取用户完整资料")
    public Map<String, Object> getUserProfile(@PathVariable int uid) {
        User user = userService.findByUID(uid);
        Map<String, Object> response = new HashMap<>();
        
        if (user != null) {
            response.put("success", true);
            response.put("uid", user.getUID());
            response.put("username", user.getName());
            response.put("nickname", user.getNickname());
            response.put("email", user.getEmail());
            response.put("avatar", user.getAvatar());
            response.put("createTime", user.getCreateTime());
            response.put("isAdmin", user.getIsadmin() == 1);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
        }
        
        return response;
    }

    // 在 UserController 类中添加以下方法（约第180行）

// 修改 getUserAchievements 方法中的类型转换部分

@GetMapping("/achievements/{uid}")
@Operation(summary = "获取用户成就完成情况")
public Map<String, Object> getUserAchievements(@PathVariable int uid) {
    try {
        Map<String, Object> response = new HashMap<>();
        
        // 获取用户基本信息
        User user = userService.findByUID(uid);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        
        // 获取成就数据
        Map<String, Object> achievementData = userService.getAchievementData(uid);
        
        // 🔧 修复：安全地转换数据类型
        int gameCount = convertToInt(achievementData.get("gameCount"));
        int winCount = convertToInt(achievementData.get("winCount"));
        int highestScore = convertToInt(achievementData.get("highestScore"));
        int collectionCount = convertToInt(achievementData.get("collectionCount"));
        int poetCount = convertToInt(achievementData.get("poetCount"));
        int commentCount = convertToInt(achievementData.get("commentCount"));
        
        // 定义9个成就
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        // 1. 初入江湖 - 完成第一场飞花令游戏
        Map<String, Object> achievement1 = new HashMap<>();
        achievement1.put("id", 1);
        achievement1.put("name", "初入江湖");
        achievement1.put("description", "完成第一场飞花令游戏");
        achievement1.put("icon", "🌱");
        achievement1.put("category", "game");
        achievement1.put("target", 1);
        achievement1.put("progress", gameCount);
        achievement1.put("unlocked", gameCount >= 1);
        achievements.add(achievement1);
        
        // 2. 游戏达人 - 完成10场飞花令游戏
        Map<String, Object> achievement2 = new HashMap<>();
        achievement2.put("id", 2);
        achievement2.put("name", "游戏达人");
        achievement2.put("description", "完成10场飞花令游戏");
        achievement2.put("icon", "🎯");
        achievement2.put("category", "game");
        achievement2.put("target", 10);
        achievement2.put("progress", gameCount);
        achievement2.put("unlocked", gameCount >= 10);
        achievements.add(achievement2);
        
        // 3. 首次胜利 - 赢得第一场飞花令游戏
        Map<String, Object> achievement3 = new HashMap<>();
        achievement3.put("id", 3);
        achievement3.put("name", "首次胜利");
        achievement3.put("description", "赢得第一场飞花令游戏");
        achievement3.put("icon", "🏆");
        achievement3.put("category", "win");
        achievement3.put("target", 1);
        achievement3.put("progress", winCount);
        achievement3.put("unlocked", winCount >= 1);
        achievements.add(achievement3);
        
        // 4. 诗词爱好者 - 收藏第一首诗词
        Map<String, Object> achievement4 = new HashMap<>();
        achievement4.put("id", 4);
        achievement4.put("name", "诗词爱好者");
        achievement4.put("description", "收藏第一首诗词");
        achievement4.put("icon", "❤️");
        achievement4.put("category", "collection");
        achievement4.put("target", 1);
        achievement4.put("progress", collectionCount);
        achievement4.put("unlocked", collectionCount >= 1);
        achievements.add(achievement4);
        
        // 5. 诗词收藏家 - 收藏10首诗词
        Map<String, Object> achievement5 = new HashMap<>();
        achievement5.put("id", 5);
        achievement5.put("name", "诗词收藏家");
        achievement5.put("description", "收藏10首诗词");
        achievement5.put("icon", "📖");
        achievement5.put("category", "collection");
        achievement5.put("target", 10);
        achievement5.put("progress", collectionCount);
        achievement5.put("unlocked", collectionCount >= 10);
        achievements.add(achievement5);
        
        // 6. 博览群书 - 收藏来自5位不同诗人的作品
        Map<String, Object> achievement6 = new HashMap<>();
        achievement6.put("id", 6);
        achievement6.put("name", "博览群书");
        achievement6.put("description", "收藏来自5位不同诗人的作品");
        achievement6.put("icon", "🎓");
        achievement6.put("category", "collection");
        achievement6.put("target", 5);
        achievement6.put("progress", poetCount);
        achievement6.put("unlocked", poetCount >= 5);
        achievements.add(achievement6);
        
        // 7. 分数新手 - 单局游戏得分超过100分
        Map<String, Object> achievement7 = new HashMap<>();
        achievement7.put("id", 7);
        achievement7.put("name", "分数新手");
        achievement7.put("description", "单局游戏得分超过100分");
        achievement7.put("icon", "📈");
        achievement7.put("category", "score");
        achievement7.put("target", 100);
        achievement7.put("progress", highestScore);
        achievement7.put("unlocked", highestScore >= 100);
        achievements.add(achievement7);
        
        // 8. 高分玩家 - 单局游戏得分超过500分
        Map<String, Object> achievement8 = new HashMap<>();
        achievement8.put("id", 8);
        achievement8.put("name", "高分玩家");
        achievement8.put("description", "单局游戏得分超过500分");
        achievement8.put("icon", "🌟");
        achievement8.put("category", "score");
        achievement8.put("target", 500);
        achievement8.put("progress", highestScore);
        achievement8.put("unlocked", highestScore >= 500);
        achievements.add(achievement8);
        
        // 9. 活跃评论者 - 发表第一条评论
        Map<String, Object> achievement9 = new HashMap<>();
        achievement9.put("id", 9);
        achievement9.put("name", "活跃评论者");
        achievement9.put("description", "发表第一条评论");
        achievement9.put("icon", "🗣️");
        achievement9.put("category", "social");
        achievement9.put("target", 1);
        achievement9.put("progress", commentCount);
        achievement9.put("unlocked", commentCount >= 1);
        achievements.add(achievement9);
        
        // 计算统计信息
        long unlockedCount = achievements.stream().mapToLong(a -> (Boolean) a.get("unlocked") ? 1 : 0).sum();
        
        response.put("success", true);
        response.put("achievements", achievements);
        response.put("totalCount", achievements.size());
        response.put("unlockedCount", unlockedCount);
        response.put("progress", Math.round((double) unlockedCount / achievements.size() * 100));
        
        return response;
        
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "获取成就失败: " + e.getMessage());
        return errorResponse;
    }
}

// 🔧 添加类型转换辅助方法
private int convertToInt(Object value) {
    if (value == null) {
        return 0;
    }
    if (value instanceof Integer) {
        return (Integer) value;
    }
    if (value instanceof Long) {
        return ((Long) value).intValue();
    }
    if (value instanceof String) {
        try {
            return Integer.parseInt((String) value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    return 0;
}
}
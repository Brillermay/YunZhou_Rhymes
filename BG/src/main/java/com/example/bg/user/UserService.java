package com.example.bg.user;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    public int addUser(User user)
    {
        User res = userMapper.findByUsername(user.getName());
        if(res!=null)return -1;
        return userMapper.UserAdd(user);
    }
    public String encryptPassword(String password, String salt) {
        return new SimpleHash("SHA-256", password, ByteSource.Util.bytes(salt),  1024).toHex();
    }
    public String login(String username,String password)
    {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        //System.out.println("Controller 层收到用户名：" + username);
        try {
            subject.login(token);
            return "登录成功";
        } catch (AuthenticationException e) {
            return "登录失败: " + e.getMessage();
        }
    }
    public int delUser(int uid)
    {
        return userMapper.delUser(uid);
    }
    public int getUID(String username)
    {
        return userMapper.getID(username);
    }
    public String getName(int uid)
    {
        return userMapper.getName(uid);
    }
    // 在 UserService.java 中添加新方法（大约在第 45 行后）
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

        // 新增：根据UID查找用户
    public User findByUID(int uid) {
        return userMapper.findByUID(uid);
    }
    
    // 新增：更新用户信息
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }

    // 在 UserService 类中添加以下方法

    public Map<String, Object> getAchievementData(int uid) {
        return userMapper.getAchievementData(uid);
    }
}

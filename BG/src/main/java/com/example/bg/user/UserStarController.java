package com.example.bg.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.bg.poem.Poem;

import java.util.List;


@RestController
@RequestMapping("/star")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true") // 🔧 添加这一行
public class UserStarController {

    @Autowired
    private UserStarMapper userStarMapper;

    // 1. 添加收藏
    @PostMapping("/add")
    public String addStar(@RequestParam int uid, @RequestParam int pid) {
        int res = userStarMapper.addStar(uid, pid);
        return res > 0 ? "收藏成功" : "收藏失败";
    }

    // 2. 取消收藏
    @DeleteMapping("/remove")
    public String removeStar(@RequestParam int uid, @RequestParam int pid) {
        int res = userStarMapper.removeStar(uid, pid);
        return res > 0 ? "取消收藏成功" : "取消收藏失败";
    }

    // 3. 查询用户所有收藏诗词PID
    @GetMapping("/list/{uid}")
    public List<Poem> getStarList(@PathVariable int uid) {
        return userStarMapper.getStarList(uid);
}
}
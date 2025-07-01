package com.example.bg.user;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {
    public int UID;
    public String name;
    public String pwd;
    // 新增字段
    public String salt; // 密码盐值
    public String status; // 账户状态

    public int isadmin;

        // 新增字段
    private String nickname;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

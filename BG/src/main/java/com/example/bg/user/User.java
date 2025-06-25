package com.example.bg.user;

import lombok.Data;

@Data
public class User {
    public int UID;
    public String name;
    public String pwd;
    // 新增字段
    public String salt; // 密码盐值
    public String status; // 账户状态
}

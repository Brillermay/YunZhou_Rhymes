package com.example.bg.user;

public interface UserLoginMapper {
    User login(User user);
    String getName(int id);
}
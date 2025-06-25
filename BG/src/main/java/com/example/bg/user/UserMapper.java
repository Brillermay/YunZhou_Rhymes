package com.example.bg.user;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {
    int UserAdd(User user);
    int delUser(int UID);
    User findByUsername(String name); // 改为按用户名查询
    String getName(int id);
    int getID(String name);
}

package com.example.bg.user;


import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;  // 🔧 添加这行导入
@Mapper
@Repository
public interface UserMapper {
    int UserAdd(User user);
    int delUser(int UID);
    User findByUsername(String name); // 改为按用户名查询
    String getName(int id);
    int getID(String name);
        // 新增：根据UID查找用户
    User findByUID(int uid);
    
    // 新增：更新用户信息
    int updateUser(User user);
    // 在 UserMapper 接口中添加以下方法

    Map<String, Object> getAchievementData(@Param("uid") int uid);
}

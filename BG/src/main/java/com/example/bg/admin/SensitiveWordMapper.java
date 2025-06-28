package com.example.bg.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface SensitiveWordMapper {

    // 获取所有启用的敏感词
    List<SensitiveWord> getActiveSensitiveWords();

    // 获取所有敏感词（用于管理）
    List<SensitiveWord> getAllSensitiveWords();

    // 添加敏感词
    void insertSensitiveWord(SensitiveWord sensitiveWord);

    // 删除敏感词
    void deleteSensitiveWord(@Param("id") int id);

    // 启用/禁用敏感词
    void updateSensitiveWordStatus(@Param("id") int id, @Param("isActive") boolean isActive);

    // 根据ID获取敏感词
    SensitiveWord getSensitiveWordById(@Param("id") int id);
}
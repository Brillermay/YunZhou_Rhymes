package com.example.bg.admin;

import com.example.bg.poem.Poem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AdminPoemMapper {

    // 获取诗词列表（分页）
    List<Poem> getPoemList(@Param("offset") int offset,
                           @Param("size") int size,
                           @Param("search") String search,
                           @Param("category") String category);

    // 获取诗词总数
    int getPoemCount(@Param("search") String search, @Param("category") String category);

    // 根据ID获取诗词
    Poem getPoemById(@Param("pid") int pid);

    // 添加诗词
    void insertPoem(Poem poem);

    // 更新诗词
    void updatePoem(Poem poem);

    // 删除诗词
    void deletePoem(@Param("pid") int pid);

    // 批量删除诗词
    void batchDeletePoems(@Param("pids") List<Integer> pids);

    // 获取所有分类
    List<String> getAllCategories();
}
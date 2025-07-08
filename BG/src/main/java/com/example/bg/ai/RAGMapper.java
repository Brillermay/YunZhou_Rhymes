package com.example.bg.ai;

import com.example.bg.poem.Poem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RAGMapper {

    // 现有方法
    List<Poem> searchPoemsByKeyword(@Param("keyword") String keyword);
    List<Poem> searchPoemsByCategory(@Param("category") String category);
    List<Poem> searchPoemsByPoet(@Param("poet") String poet);
    List<Poem> getAllPoems();
    List<Poem> getRandomPoems(@Param("limit") int limit);

    // 🆕 新增方法：用于AI搜索
    /**
     * 根据PID查询单首诗词
     */
    Poem getPoemByPID(@Param("poemId") String poemId);

    /**
     * 根据多个PID批量查询诗词
     */
    List<Poem> getPoemsByPIDs(@Param("list") List<String> poemIds);

    /**
     * 检查PID是否存在
     */
    Boolean checkPoemExists(@Param("poemId") String poemId);

    /**
     * 获取诗词总数
     */
    Integer countAllPoems();

    /**
     * 获取所有诗词（用于RAG初始化）
     */
    List<Poem> getAllPoemsForRAG();

    /**
     * 🆕 根据标题和作者查询诗词
     */
    Poem getPoemByTitleAndPoet(@Param("title") String title, @Param("poet") String poet);

    /**
     * 🆕 根据标题查询诗词（模糊匹配）
     */
    List<Poem> getPoemsByTitle(@Param("title") String title);

        // 查询用户收藏诗词
    List<Poem> getFavoritePoemsByUserId(String userId);

    // 根据AI分析结果推荐诗词（这里简单用主题模糊查找）
    List<Poem> recommendPoemsByTheme(String theme, int limit);
}
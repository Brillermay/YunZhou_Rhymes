package com.example.bg.poem;

import java.util.List;

//查找关键字:getPoems来查找

public interface PoemGetMapper {
    Poem getPoem(int PID);
    List<Poem> getPoems(String key);
    
    // RAG功能所需的方法
    List<Poem> searchPoemsByKeyword(String keyword);
    List<Poem> searchPoemsByCategory(String category);
    List<Poem> searchPoemsByPoet(String poet);
    List<Poem> getAllPoems();
    List<Poem> getRandomPoems(int limit);
}
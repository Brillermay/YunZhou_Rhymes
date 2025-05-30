package com.example.bg.poem;

import java.util.List;

//查找关键字:getPoems来查找

public interface PoemGetMapper {
    Poem getPoem(int PID);
    List<Poem> getPoems(String key);
}

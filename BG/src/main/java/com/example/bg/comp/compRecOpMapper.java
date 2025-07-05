package com.example.bg.comp;

//查找：getHistory(int UID)，根据传入的UID到数据表中查找对应的记录并返回类
//更新：updateHistory(compRec rec);根据传入的rec进行更新
//添加：addHistory(compRec rec);添加
import java.util.List;
import java.util.Map;

public interface compRecOpMapper {
    compRec getHistory(int UID, int Difficulty, int Sum);
    void updateHistory(compRec rec);
    void addHistory(compRec rec);
    List<compRec> getRank(int Difficulty, int Sum);
        // 🔧 新增方法
    Map<String, Object> getUserStats(int UID);
    void insertUserStats(Map<String, Object> stats);
    void updateUserStats(Map<String, Object> stats);
}

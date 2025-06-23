package com.example.bg.comp;

//查找：getHistory(int UID)，根据传入的UID到数据表中查找对应的记录并返回类
//更新：updateHistory(compRec rec);根据传入的rec进行更新
//添加：addHistory(compRec rec);添加
import java.util.List;

public interface compRecOpMapper {
    List<compRec>getHistory(int UID);
    void updateHistory(compRec rec);
    void addHistory(compRec rec);
}

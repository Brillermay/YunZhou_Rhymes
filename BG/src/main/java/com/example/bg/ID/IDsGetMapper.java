package com.example.bg.ID;

public interface IDsGetMapper {
    int getCID();//返回单个CID，用来给Comment计数
    void addStarID(int PID,int UID);
    void delStarID(int PID,int UID);
}

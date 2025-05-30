package com.example.bg.ID;

public interface IDsGetMapper {
    int getCID();//返回单个CID，用来给Comment计数
    void  addStarID(int x,int UID);
    void delStarID(int x,int UID);
}

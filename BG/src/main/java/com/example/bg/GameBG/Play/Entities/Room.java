package com.example.bg.GameBG.Play.Entities;


import lombok.Data;

@Data
public class Room {
    /*
    * uid1：玩家1uid
    * uid2：玩家2uid
    * room：房间号，16位随机字符串
    * roundNum：回合数
    * */
    private int uid1,uid2;
    private String room;
    private int roundNum;


}

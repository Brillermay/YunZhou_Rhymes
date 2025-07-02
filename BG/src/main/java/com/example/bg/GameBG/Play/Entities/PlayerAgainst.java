package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

import java.util.List;
@Data
public class PlayerAgainst {
    /*
    * 传递过来的类：
    * 包含字段：
    * hp，int，血条
    * room，string，房间号
    * cards，list<CardBattle>，手牌
    * wealthy,int，金钱数
    * statuses,List<Status>，状态;
    * role,string,选择的角色名称
    * shield,int,护盾量
    * */
    private int hp;
    private String roomNumber;
    private List<CardBattle>cards;
    private int wealthy;
    private List<Status>statuses;
    private String role;
    private int shield;
}

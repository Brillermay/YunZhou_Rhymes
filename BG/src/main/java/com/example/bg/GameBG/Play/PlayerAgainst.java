package com.example.bg.GameBG.Play;

import lombok.Data;

import java.awt.print.PrinterGraphics;
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
    * */
    private int hp;
    private String roomNumber;
    private List<CardBattle>cards;
    private int wealthy;
    private List<Status>statuses;
}

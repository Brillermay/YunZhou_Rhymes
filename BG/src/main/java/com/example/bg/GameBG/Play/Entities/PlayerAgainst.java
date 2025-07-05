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
    * statusesEnd,List<Status>，回合结束时获得的状态;
    * statusesBegin,List<Status>,回合开始时进行结算的状态；
    * role,string,选择的角色名称
    * shield,int,护盾量
    * */
    private int hp;
    private String roomNumber;
    private List<CardBattle>cards;
    private int wealthy;
    private List<Status> statusesEnd;
    private List<Status>statusesBegin;
    private String role;
    private int shield;
    private int hpMax,shieldMax;

    private PlayerCondition playerCondition;



    private int cardsNum;
    public int countCardsNum()
    {
        int ans=0;
        for(CardBattle cardBattle:cards){
            ans+=cardBattle.getCardNum();
        }
        return ans;
    }

}

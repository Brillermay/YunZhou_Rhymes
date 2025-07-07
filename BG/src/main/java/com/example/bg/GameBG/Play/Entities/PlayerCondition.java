package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

@Data
public class PlayerCondition {
    public PlayerCondition(){
        isAttacked=false;
        isUsedBattleCard=false;
        isUsedDefenseCard=false;
        isUsedProfitOrDecreaseCard=false;
        isRobbedHP=false;
        isAddedShield=false;
        isCured=false;
        hasRain=false;
        hasZhuangZhiNanChou=false;
        hasWar=false;
        canAddShield=true;
        liuBuff=false;
        sunBuff=1;
        natureBuff=false;
        numOfShieldAdd=0;
        fireAdd=0;
        purification=false;
    }


    private boolean isAttacked;

    private boolean isUsedDefenseCard;
    private boolean isUsedBattleCard;
    private boolean isUsedProfitOrDecreaseCard;

    private boolean isRobbedHP;
    private boolean isAddedShield;
    private boolean isCured;


    private boolean hasZhuangZhiNanChou;
    private boolean hasWar;
    private boolean hasRain;

    private boolean canAddShield;
    private boolean liuBuff;
    private int sunBuff;
    private boolean natureBuff;
    private int numOfShieldAdd;
    private int fireAdd;
    private boolean purification;
}

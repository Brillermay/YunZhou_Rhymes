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
    }


    private boolean isAttacked;

    private boolean isUsedDefenseCard;
    private boolean isUsedBattleCard;
    private boolean isUsedProfitOrDecreaseCard;

    private boolean isRobbedHP;
    private boolean isAddedShield;
    private boolean isCured;

}

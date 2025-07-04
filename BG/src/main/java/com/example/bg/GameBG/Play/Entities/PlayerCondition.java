package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

@Data
public class PlayerCondition {
    boolean isAttacked;

    boolean isUsedDefenseCard;
    boolean isUsedBattleCard;
    boolean isUsedProfitOrDecreaseCard;

    boolean isRobbedHP;
    boolean isAddedShield;

}

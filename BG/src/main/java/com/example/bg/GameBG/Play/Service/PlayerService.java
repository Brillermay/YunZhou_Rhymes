package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.BattleMapper;
import com.example.bg.GameBG.Play.Entities.CardBattle;
import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
import com.example.bg.GameBG.Play.Entities.Status;
import com.example.bg.GameBG.Player.Player;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlayerService {
    /*
    * 这里进行各种对战结算函数
    * 分成总MainService，里面用switch切换到各种，最后返回user
    * 还有一个关键点就是：这里只负责处理
    * */




    //几个原子操作
    CardService cardService=new CardService();
    /**
     * 为玩家增加或减少金币
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
     * @param coins 要增加的金币数量，正数表示增加，负数表示减少
     */
    public void AddCoins(PlayerAgainst playerAgainst, int coins) {
        //修改player的金币
        playerAgainst.setWealthy(playerAgainst.getWealthy() + coins);
    }

    /**
     * 为玩家增加或减少血量
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
     * @param hp 要增加的血量，正数表示恢复血量，负数表示扣除血量
     */
    public void AddHP(PlayerAgainst playerAgainst, int hp) {
        //修改player的血量
        playerAgainst.setHp(playerAgainst.getHp() + hp);
    }

    /**
     * 为玩家增加或减少护盾
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
     * @param shield 要增加的护盾数量，正数表示增加护盾，负数表示减少护盾
     */
    public void AddShield(PlayerAgainst playerAgainst, int shield) {
        //修改player的护盾
        playerAgainst.setShield(playerAgainst.getShield() + shield);
    }

    /**
     * 修改卡牌的面值大小
     * @param cardBattle 要修改的卡牌对象（注意：此方法会直接修改传入的对象）
     * @param delta 面值变化量，正数表示增加面值，负数表示减少面值
     */
    public void ModifyCardSize(CardBattle cardBattle, int delta) {
        //修改卡牌的牌面大小
        cardBattle.setCardSize(cardBattle.getCardSize() + delta);
    }

    /**
     * 为玩家添加随机卡牌
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的卡牌列表）
     * @param addNum 要添加的卡牌数量，如果小于等于0则不添加任何卡牌
     * @param costs 卡牌面值上限，如果为-1则不限制面值
     */
    public void AddPlayerCards(PlayerAgainst playerAgainst, int addNum, int costs) {
        //给用户添加卡牌
        List<CardBattle> old = playerAgainst.getCards();
        List<CardBattle> added = cardService.RandomGetCardsByNumAndCost(addNum, costs);
        playerAgainst.setCards(cardService.MergeCardList(old, added));
    }

    /**
     * 随机丢弃玩家的卡牌
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的卡牌列表）
     * @param num 要丢弃的卡牌数量，如果小于等于0则不丢弃任何卡牌，如果大于玩家拥有的卡牌总数则丢弃全部卡牌
     */
    public void DiscardPlayerCards(PlayerAgainst playerAgainst, int num) {
        //给用户随机删除num张卡牌
        playerAgainst.setCards(cardService.RandomDiscardCardsList(playerAgainst.getCards(), num));
    }

    /**
     * 为玩家添加状态效果
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的状态列表）
     * @param statuses 要添加的状态列表，如果为null或空列表则不添加任何状态
     */
    public void AddStatus(PlayerAgainst playerAgainst, List<Status> statuses) {
        // 空值检查
        if (statuses == null || statuses.isEmpty()) {
            return;
        }

        // 确保玩家的状态列表不为null
        if (playerAgainst.getStatuses() == null) {
            playerAgainst.setStatuses(new ArrayList<>());
        }

        // 添加所有状态
        playerAgainst.getStatuses().addAll(statuses);
    }

    //结算status
    public void CalStatus(PlayerAgainst playerAgainst){}
}

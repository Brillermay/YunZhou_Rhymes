package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.BattleMapper;
import com.example.bg.GameBG.Play.Entities.CardBattle;
import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
import com.example.bg.GameBG.Play.Entities.Status;
import com.example.bg.GameBG.Player.Player;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface CardAction{
    void execute(PlayerAgainst player, CardBattle card);
}

public class PlayerService {
    /*
    * 这里进行各种对战结算函数
    * 分成总MainService，里面用switch切换到各种，最后返回user
    * 还有一个关键点就是：这里只负责处理
    * */
    //这个的设计思路就是只传受影响的玩家和影响他的卡牌
    private final Map<String, CardAction> cardActions = new HashMap<>();

    private void initializeCardActions() {
        cardActions.put("spring", this::Action_Spring);
        cardActions.put("fire", this::Action_Fire);
        cardActions.put("bird", this::Action_Bird);
        cardActions.put("autumn", this::Action_Autumn);
        cardActions.put("mountain", this::Action_Mountain);
        cardActions.put("water", this::Action_Water);
        cardActions.put("moon", this::Action_Moon);
        cardActions.put("sad", this::Action_Sad);
        cardActions.put("home", this::Action_Home);
        cardActions.put("wine", this::Action_Wine);
        cardActions.put("liu", this::Action_Liu);
        cardActions.put("sun", this::Action_Sun);
        cardActions.put("goose", this::Action_Goose);
        cardActions.put("friend", this::Action_Friend);
        cardActions.put("rain", this::Action_Rain);
        cardActions.put("war", this::Action_War);
        cardActions.put("nature", this::Action_Nature);
        cardActions.put("byebye", this::Action_Byebye);
        cardActions.put("flower", this::Action_Flower);
        cardActions.put("bamboo", this::Action_Bamboo);
        cardActions.put("zhuangzhinanchou", this::Action_Zhuangzhinanchou);
        cardActions.put("danbo", this::Action_Danbo);
        cardActions.put("yellowriver", this::Action_Yellowriver);
        cardActions.put("missing", this::Action_Missing);
        cardActions.put("longriver", this::Action_Longriver);
        cardActions.put("love", this::Action_Love);
    }

    private void Action_Spring(PlayerAgainst playerAgainst, CardBattle cardBattle) {
        //实现效果：恢复1点血量。若本回合未受伤害，下回合获得3金币，且抽1张牌。

    }
    private void Action_Fire(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Bird(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Autumn(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Mountain(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Water(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Moon(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Sad(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Home(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Wine(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Liu(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Sun(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Goose(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Friend(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Rain(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_War(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Nature(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Byebye(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Flower(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Bamboo(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Zhuangzhinanchou(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Danbo(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Yellowriver(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Missing(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Longriver(PlayerAgainst playerAgainst, CardBattle cardBattle) {}
    private void Action_Love(PlayerAgainst playerAgainst, CardBattle cardBattle){}
    public PlayerService(){
        initializeCardActions();
    }

    /**
     * 主服务方法：根据卡牌名称执行对应的动作
     * @param playerAgainst 目标玩家
     * @param cardBattle 使用的卡牌
     */
    public void MainService(PlayerAgainst playerAgainst,CardBattle cardBattle)
    {
        String cardName = cardBattle.getCardName();
        CardAction action = cardActions.get(cardName);

        if (action != null) {
            action.execute(playerAgainst, cardBattle);
        } else {
            // 处理未知卡牌
            System.out.println("未知卡牌: " + cardName);
        }

    }




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

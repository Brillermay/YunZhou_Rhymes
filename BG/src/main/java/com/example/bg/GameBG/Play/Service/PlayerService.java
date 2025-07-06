package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.*;
import io.netty.channel.ConnectTimeoutException;
import org.apache.poi.ss.formula.functions.Roman;
import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
import org.springframework.cglib.transform.impl.AddStaticInitTransformer;

import javax.print.DocFlavor;
import java.util.*;

import static java.lang.Math.max;

/**
 * 卡牌动作接口，定义卡牌效果的执行方法
 */
interface CardAction{
    /**
     * 执行卡牌效果
     * @param user 使用卡牌的玩家
     * @param target 目标玩家
     * @param card 使用的卡牌
     */
    void execute(PlayerAgainst user,PlayerAgainst target, CardBattle card);
}


/**
 * BUFF动作接口，定义BUFF效果的执行方法
 */
interface BuffAction{
    /**
     * 执行BUFF效果
     * @param user 拥有BUFF的玩家
     * @param target 目标玩家
     */
    void execute(PlayerAgainst user,PlayerAgainst target);
}

/**
 * 适配回合开始前的 BUFF结算动作接口，定义NEXT_BUFF效果的执行方法
 */
interface NextBuffAction{
    /**
     * 执行NEXT_BUFF效果
     * @param user 拥有BUFF的玩家
     * @param target 目标玩家
     * @param listPlayer1 玩家1出的手牌
     * @param listPlayer2 玩家2出的手牌
     * @param buffName 玩家1的buff
     */
    void execute(PlayerAgainst user,PlayerAgainst target,
                 List<CardBattle>listPlayer1,List<CardBattle>listPlayer2,
                 String buffName);
}



/**
 * 玩家服务类，负责处理游戏中的各种对战结算功能
 * 主要包含卡牌效果处理、状态管理、玩家属性修改等核心功能
 */
public class PlayerService {
    /*
     * 这里进行各种对战结算函数
     * 分成总MainService，里面用switch切换到各种，最后返回user
     * 还有一个关键点就是：这里只负责处理
     * 对于状态：所有状态一律加到statusesEnd中，在结束的Main里处理End为begin，begin才是真正有用的buff
     *
     * */
    /**
     * 构造函数，初始化卡牌动作映射
     */
    public PlayerService(){
        initializeCardActions();
        initializeBuffActions();
        initializeNextBuffActions();
    }

    public String OpenCards(PlayerAgainst playerAgainst){
        if(playerAgainst.getWealthy()<5){
            return"没有足够的金币";
        }
        AddCoins(playerAgainst,-5);
        List<CardBattle>list=cardService.RandomGetCardsByNumAndCost(5,1);
        list= cardService.MergeCardList(playerAgainst.getCards(),list);
        playerAgainst.setCards(list);
        return "购买成功";
    }

    //两个功能函数：合成和丢弃已经出的卡牌
    /**
     * 卡牌合成功能：消耗两种材料卡牌，生成一种目标卡牌
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的卡牌列表）
     * @param a 第一种材料卡牌名称
     * @param b 第二种材料卡牌名称
     * @param c 目标合成卡牌名称
     */
    public void SynthesizeABC(PlayerAgainst playerAgainst,String a,String b,String c){
        playerAgainst.setCards(cardService.DiscardCard(playerAgainst.getCards(),a));
        playerAgainst.setCards(cardService.DiscardCard(playerAgainst.getCards(),b));
        playerAgainst.setCards(cardService.GetCardByName(playerAgainst.getCards(),c));

    }
    /**
     * 卡牌丢弃：丢弃掉已经用了的卡牌
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的卡牌列表）
     * @param cardBattleList 使用的卡牌列表
     */
    public void DiscardPlayersCards(PlayerAgainst playerAgainst,List<CardBattle>cardBattleList){
        for(CardBattle cardBattle:cardBattleList){
            playerAgainst.setCards(cardService.DiscardCard(playerAgainst.getCards(),cardBattle.getCardName()));
        }
    }
    /**
     * 对玩家卡牌列表按类型优先级排序
     * 优先级规则：profit/decrease → defense → battle
     *
     * @param listPlayer1 要排序的卡牌列表（原地修改）
     */
    public static void sortCardBattleByPriority(List<CardBattle> listPlayer1) {
        // 定义类型权重映射（profit/decrease=0, defense=1, battle=2）
        listPlayer1.sort(Comparator.comparingInt(card  -> {
            if (card == null || card.getCardType()  == null) return 3;
            return switch (card.getCardType())  {
                case "profit", "decrease" -> 0; // 最高权重
                case "defense" -> 1;
                case "battle" -> 2;
                default -> 3; // 未知类型兜底
            };
        }));
    }

    //一个顶端流程调度函数
    //三个流程实现函数
    public List<CardBattle> GetList(List<String>lst){
        List<CardBattle>ans=new ArrayList<>();
        for(String s: lst) {
            ans = cardService.GetCardByName(ans, s);
        }
        return ans;
    }

    /**
     * 主服务方法，处理回合内的卡牌使用
     * @param playerAgainst1 玩家1
     * @param playerAgainst2 玩家2
     * @param listPlayer1 玩家1本回合出牌列表
     * @param listPlayer2 玩家2本回合出牌列表
     */
    public void MainService(PlayerAgainst playerAgainst1, PlayerAgainst playerAgainst2,
                            List<CardBattle>listPlayer1, List<CardBattle>listPlayer2,
                            Room room){
//        if(room.getRoundNum()%3 == 0 && room.getRoundNum()!=0)
//        {
//            AddShield(playerAgainst1,1);
//            AddShield(playerAgainst2,1);
//        }
//        room.setRoundNum(room.getRoundNum()+1);
        BeginService(playerAgainst1,playerAgainst2,
                listPlayer1,listPlayer2,true);
        //接受的是本回合出牌列表
        //首先先丢弃
        DiscardPlayersCards(playerAgainst1,listPlayer1);
        DiscardPlayersCards(playerAgainst2,listPlayer2);
        //接着这样排序
        sortCardBattleByPriority(listPlayer1);
        sortCardBattleByPriority(listPlayer2);
        for(int i=0;i<3;i++){
            MainOpService(playerAgainst1,playerAgainst2,listPlayer1.get(i));
            MainOpService(playerAgainst2,playerAgainst1,listPlayer2.get(i));
        }

        EndService(playerAgainst1,
                playerAgainst2);
        playerAgainst1.setPlayerCondition(new PlayerCondition());
        playerAgainst2.setPlayerCondition(new PlayerCondition());

    }
    /**
     * 主服务方法：根据buff名称执行对应的动作
     * @param influencer 受影响者
     * @param optimizer 对立者
     * @param buffName buff名称
     */
    public void MainBuffService(PlayerAgainst influencer,PlayerAgainst optimizer,String buffName)
    {
        BuffAction action = buffActions.get(buffName);
        if (action != null) {
            action.execute(influencer,optimizer);
        }

    }
    /**
     * 执行NEXT_BUFF效果
     * @param user 拥有BUFF的玩家
     * @param target 目标玩家
     * @param listPlayer1 玩家1出的手牌
     * @param listPlayer2 玩家2出的手牌
     * @param buffName 玩家1的buff
     */
    public void MainNextBuffService(PlayerAgainst user, PlayerAgainst target,
                                    List<CardBattle>listPlayer1, List<CardBattle>listPlayer2, String buffName)
    {
        NextBuffAction action = nextBuffActionMap.get(buffName);
        if (action != null) {
            action.execute(user,target,listPlayer1,listPlayer2,buffName);
        }

    }


    /**
     * 主服务方法：根据卡牌名称执行对应的动作
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    public void MainOpService(PlayerAgainst user,PlayerAgainst target,CardBattle cardBattle)
    {
        String cardName = cardBattle.getCardName();
        if(cardName.isEmpty())return;
        CardAction action = cardActions.get(cardName);

        if (action != null) {
            action.execute(user,target, cardBattle);
        }

    }

    /*
    * EndService:
    *   对包含judge的：调用MainBuffService
    *   其他的，直接放到statusesBegin
    * */

    /**
     * 更新状态持续时间并移除过期状态
     * @param statusList 要处理的状态列表（原地修改）
     */
    public static void updateAndCleanStatuses(List<Status> statusList) {
        if (statusList == null || statusList.isEmpty())  return;

        Iterator<Status> iterator = statusList.iterator();
        while (iterator.hasNext())  {
            Status status = iterator.next();
            if (status == null) {
                iterator.remove();
                continue;
            }

            int newTime = status.getConsistTime()  - 1;
            status.setConsistTime(newTime);

            if (newTime <= 0) {
                iterator.remove();
                // 可添加状态移除回调：onStatusExpired(status);
            }
        }
    }


    /**
     * 回合结束处理服务
     * @param playerAgainst1 玩家1
     * @param playerAgainst2 玩家2
     */
    public void EndService(PlayerAgainst playerAgainst1,PlayerAgainst playerAgainst2) {
        updateAndCleanStatuses(playerAgainst1.getStatusesBegin());
        updateAndCleanStatuses(playerAgainst2.getStatusesBegin());
        /*
        * 专门处理特殊buff
        * */
        //处理壮志难酬zhuangzhinanchou的buff
        if(playerAgainst1.getPlayerCondition().isHasZhuangZhiNanChou())
            AddShield(playerAgainst1,playerAgainst2.getPlayerCondition().getNumOfShieldAdd());
        if(playerAgainst2.getPlayerCondition().isHasZhuangZhiNanChou())
            AddShield(playerAgainst2,playerAgainst1.getPlayerCondition().getNumOfShieldAdd());

        for(Status status:playerAgainst1.getStatusesEnd())
        {
            if(status.getName().contains("judge"))
                MainBuffService(playerAgainst1,playerAgainst2,status.getName());
            else
                playerAgainst1.getStatusesBegin().add(status);

        }
        playerAgainst1.setStatusesEnd(new ArrayList<>());
        for(Status status: playerAgainst2.getStatusesEnd())
        {
            if(status.getName().contains("judge"))
                MainBuffService(playerAgainst2,playerAgainst1,status.getName());
            else
                playerAgainst2.getStatusesBegin().add(status);
        }
        playerAgainst2.setStatusesEnd(new ArrayList<>());
    }

    /**
     * 回合结束处理服务
     * @param playerAgainst1 玩家1
     * @param playerAgainst2 玩家2
     * @param listPlayer1 玩家1本回合出牌列表
     * @param listPlayer2 玩家2本回合出牌列表
     */
    public void BeginService(PlayerAgainst playerAgainst1,PlayerAgainst playerAgainst2,
                             List<CardBattle>listPlayer1,List<CardBattle>listPlayer2,boolean goer) {
        if(!goer) {
            AddCoins(playerAgainst1, 5);
            AddCoins(playerAgainst2, 5);
        }
        for(Status status:playerAgainst1.getStatusesBegin())
        {
            if(status.getName().contains("next"))
            {
                if(!goer) continue;
                MainNextBuffService(playerAgainst1,playerAgainst2,
                        listPlayer1,listPlayer2,status.getName());
            }
            else if(!goer)
                MainBuffService(playerAgainst1,playerAgainst2,status.getName());
        }
        for(Status status:playerAgainst2.getStatusesBegin())
        {
            if(status.getName().contains("next"))
            {
                if(!goer) continue;
                MainNextBuffService(playerAgainst2,playerAgainst1,
                        listPlayer1,listPlayer2,status.getName());
            }
            else if(!goer)
                MainBuffService(playerAgainst2,playerAgainst1,status.getName());
        }
    }



    //这一部分是卡牌效果
    //这个的设计思路就是只传受影响的玩家和影响他的卡牌
    /**
     * NEXT_BUFF动作映射表，存储BUFF名称与对应处理方法的映射关系
     */
    private final Map<String,NextBuffAction>nextBuffActionMap=new HashMap<>();
    /**
     * 初始化NEXT_BUFF动作映射表，将所有BUFF名称与对应的处理方法关联
     */
    private void initializeNextBuffActions() {

        nextBuffActionMap.put("rain_next", this::BuffAction_rain_next);
        nextBuffActionMap.put("war_next", this::BuffAction_war_next);
        nextBuffActionMap.put("zhuangzhinanchou_next", this::BuffAction_zhuangzhinanchou_next);

    }

    /**
     * BUFF动作映射表，存储BUFF名称与对应处理方法的映射关系
     */
    private final Map<String,BuffAction>buffActions=new HashMap<>();
    /**
     * 初始化BUFF动作映射表，将所有BUFF名称与对应的处理方法关联
     */
    private void initializeBuffActions() {
        buffActions.put("spring", this::BuffAction_spring);
        buffActions.put("fire", this::BuffAction_fire);
        buffActions.put("bird", this::BuffAction_bird);
        buffActions.put("autumn", this::BuffAction_autumn);
        buffActions.put("mountain", this::BuffAction_mountain);
        buffActions.put("water", this::BuffAction_water);
        buffActions.put("sad", this::BuffAction_sad);
        buffActions.put("home", this::BuffAction_home);
        buffActions.put("wine", this::BuffAction_wine);
        buffActions.put("liu", this::BuffAction_liu);
        buffActions.put("sun", this::BuffAction_sun);
        buffActions.put("goose", this::BuffAction_goose);
        buffActions.put("friend", this::BuffAction_friend);
        buffActions.put("rain", this::BuffAction_rain);
        buffActions.put("war", this::BuffAction_war);
        buffActions.put("nature", this::BuffAction_nature);
        buffActions.put("byebye", this::BuffAction_byebye);
        buffActions.put("flower", this::BuffAction_flower);
        buffActions.put("bamboo", this::BuffAction_bamboo);
        buffActions.put("zhuangzhinanchou", this::BuffAction_zhuangzhinanchou);
        buffActions.put("danbo", this::BuffAction_danbo);
        buffActions.put("yellowriver", this::BuffAction_yellowriver);
        buffActions.put("longriver", this::BuffAction_longriver);
        buffActions.put("love", this::BuffAction_love);
        buffActions.put("spring_judge", this::BuffAction_spring_judge);
        buffActions.put("bird_judge", this::BuffAction_bird_judge);
        buffActions.put("autumn_judge", this::BuffAction_autumn_judge);
        buffActions.put("mountain_judge", this::BuffAction_mountain_judge);
        buffActions.put("water_judge", this::BuffAction_water_judge);
        buffActions.put("wine_judge", this::BuffAction_wine_judge);
        buffActions.put("liu_judge", this::BuffAction_liu_judge);
        buffActions.put("bamboo_judge", this::BuffAction_bamboo_judge);
        buffActions.put("danbo_judge", this::BuffAction_danbo_judge);
    }


    /**
     * 春天Buff：回合开始时获得3金币，且抽1张牌。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_spring(PlayerAgainst user, PlayerAgainst target) {
        //回合开始时获得3金币，且抽1张牌
        AddCoins(user,3);
        user.setCards(cardService.RandomGetCardsByNumAndCost(1,3));
    }

    /**
     * 火焰Buff：下回合抽1张牌并使其下回合战斗类卡牌费用+1。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_fire(PlayerAgainst user, PlayerAgainst target) {
        //下回合抽1张牌并使其下回合战斗类卡牌费用+1
        user.setCards(cardService.RandomGetCardsByNumAndCost(1,2));
        user.getPlayerCondition().setFireAdd(user.getPlayerCondition().getFireAdd()+1);
    }

    /**
     * 鸟Buff：若本回合对面使用防守类卡，追加1点真实伤害。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_bird(PlayerAgainst user, PlayerAgainst target) {
        //若本回合对面使用防守类卡，追加1点真实伤害。
    }

    /**
     * 秋天Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_autumn(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 山Buff：获得1点护盾。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_mountain(PlayerAgainst user, PlayerAgainst target) {
        AddShield(user,1);
    }

    /**
     * 水Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_water(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 悲伤Buff：无法获得护盾。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_sad(PlayerAgainst user, PlayerAgainst target) {
        //无法获得护盾
        user.getPlayerCondition().setCanAddShield(false);
    }

    /**
     * 家Buff：下回合抽1张牌。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_home(PlayerAgainst user, PlayerAgainst target) {
        //下回合抽1张牌
        List<CardBattle> list = cardService.RandomGetCardsByNumAndCost(1,2);
        list = cardService.MergeCardList(list,user.getCards());
        user.setCards(list);
    }

    /**
     * 酒Buff：抽2张牌，下回合战斗类卡牌费用+1。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_wine(PlayerAgainst user, PlayerAgainst target) {
        List<CardBattle>list=cardService.RandomGetCardsByNumAndCost(2,2);
        user.setCards(cardService.MergeCardList(list,user.getCards()));
        user.getPlayerCondition().setFireAdd(user.getPlayerCondition().getFireAdd()+1);
    }

    /**
     * 柳Buff：获得柳Buff效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_liu(PlayerAgainst user, PlayerAgainst target) {
        user.getPlayerCondition().setLiuBuff(true);
    }

    /**
     * 太阳Buff：防守卡牌增益减半。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_sun(PlayerAgainst user, PlayerAgainst target) {
        //防守卡牌增益减半
        user.getPlayerCondition().setSunBuff(2);
    }

    /**
     * 鹅Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_goose(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 朋友Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_friend(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 雨Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_rain(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 战争Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_war(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 自然Buff：免疫1次破盾的额外伤害。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_nature(PlayerAgainst user, PlayerAgainst target) {
        //免疫1次破盾的额外伤害。
        user.getPlayerCondition().setNatureBuff(true);
    }

    /**
     * 再见Buff：下两回合获得金币-2。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_byebye(PlayerAgainst user, PlayerAgainst target) {
        //下两回合获得金币-2
        AddCoins(user,-2);
    }

    /**
     * 花Buff：恢复3点血量，下三回合各获得2点护盾且每回合回1点血。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_flower(PlayerAgainst user, PlayerAgainst target) {
        //恢复3点血量，下三回合各获得2点护盾且每回合回1点血。
        AddShield(user,2);
        AddHP(user,1);
    }

    /**
     * 竹Buff：下回合开始时抽3张牌并破坏对手1点护盾。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_bamboo(PlayerAgainst user, PlayerAgainst target) {
        //下回合开始时抽3张牌并破坏对手1点护盾
        user.setCards(cardService.MergeCardList(cardService.RandomGetCardsByNumAndCost(3,2),user.getCards()));
        killShield(target,-1);
    }

    /**
     * 壮志难酬Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_zhuangzhinanchou(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 淡泊Buff：战斗类牌面值减2。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_danbo(PlayerAgainst user, PlayerAgainst target) {
        //战斗类牌面值减2
        user.getPlayerCondition().setFireAdd(user.getPlayerCondition().getFireAdd()-2);
    }

    /**
     * 黄河Buff：暂无效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_yellowriver(PlayerAgainst user, PlayerAgainst target) {}

    /**
     * 长江Buff：下三回合每回合恢复4点血量。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_longriver(PlayerAgainst user, PlayerAgainst target) {
        //下三回合每回合恢复4点血量。
        AddHP(user,4);
    }

    /**
     * 爱Buff：获得3点护盾，失去3金币，获得净化效果。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_love(PlayerAgainst user, PlayerAgainst target) {
        //
        AddShield(user,3);
        AddCoins(user,-3);
        user.getPlayerCondition().setPurification(true);
    }

    /**
     * 春天判定Buff：若本回合未受攻击，下回合开始时获得3金币，且抽1张牌。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_spring_judge(PlayerAgainst user, PlayerAgainst target) {
        //若本回合未受攻击，下回合开始时获得3金币，且抽1张牌。
        if(user.getPlayerCondition().isAttacked()){
            user.getStatusesBegin().add(new Status("spring",1,"回合开始时获得3金币，且抽1张牌"));
        }
    }

    /**
     * 鸟判定Buff：若本回合对面使用防守类卡，追加1点真实伤害。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_bird_judge(PlayerAgainst user, PlayerAgainst target) {
        //若本回合对面使用防守类卡，追加1点真实伤害。
        if(target.getPlayerCondition().isUsedDefenseCard())
            AddHP(target,-1);
    }

    /**
     * 秋天判定Buff：若其本回合未获得新护盾，弃其1张牌并让他失去1点护盾。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_autumn_judge(PlayerAgainst user, PlayerAgainst target) {
        //若其本回合未获得新护盾，弃其1张牌并让他失去1点护盾。
        if(user.getPlayerCondition().isAddedShield())
        {
            return ;
        }
        AddShield(user,-1);
        user.setCards(cardService.RandomDiscardCardsList(user.getCards(),1));
    }

    /**
     * 山判定Buff：若本回合未受伤害，下三回合各+1护盾。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_mountain_judge(PlayerAgainst user, PlayerAgainst target) {
        //若本回合未受伤害，下三回合各+1护盾。
        if(user.getPlayerCondition().isAttacked() == false)
        {
            user.getStatusesBegin().add(new Status("mountain",3,"每回合开始时护盾+1"));
        }
    }

    /**
     * 水判定Buff：获得1点护盾。若未使用战斗类卡牌，恢复2点血量。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_water_judge(PlayerAgainst user, PlayerAgainst target) {
        //获得1点护盾。若未使用战斗类卡牌，恢复2点血量。
        if(user.getPlayerCondition().isUsedBattleCard())return;
        AddHP(user,2);
    }

    /**
     * 酒判定Buff：若本回合受到攻击，抽2张牌且下回合战斗类卡牌费用+1。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_wine_judge(PlayerAgainst user, PlayerAgainst target) {
        //若本回合受到攻击，抽2张牌且下回合战斗类卡牌费用+1。
        if(user.getPlayerCondition().isAttacked()){
            user.getStatusesBegin().add(new Status("wine",1,"下回合开局抽2张1-2费牌,且战斗类卡牌效果+1"));
        }
    }

    /**
     * 柳判定Buff：若本回合受到攻击，恢复2点护盾并免疫下回合1点伤害。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_liu_judge(PlayerAgainst user, PlayerAgainst target) {
        //若本回合受到攻击，恢复2点护盾并免疫下回合1点伤害（免疫破盾后打到肉的1点）。
        if(user.getPlayerCondition().isAttacked())
        {
            AddShield(user,2);
            user.getStatusesBegin().add(new Status("liu",1,"免疫下回合1点伤害（免疫破盾后打到肉的1点）"));
        }
    }

    /**
     * 雨Buff（下回合）：设置雨Buff标记。
     * @param playerAgainst1 玩家1
     * @param playerAgainst2 玩家2
     * @param listPlayer1 玩家1卡牌列表
     * @param listPlayer2 玩家2卡牌列表
     * @param buffName Buff名称
     */
    private void BuffAction_rain_next(PlayerAgainst playerAgainst1, PlayerAgainst playerAgainst2,
                                      List<CardBattle>listPlayer1,List<CardBattle>listPlayer2,
                                      String buffName) {
        playerAgainst1.getPlayerCondition().setHasRain(true);
    }

    /**
     * 战争Buff（下回合）：设置战争Buff标记。
     * @param playerAgainst1 玩家1
     * @param playerAgainst2 玩家2
     * @param listPlayer1 玩家1卡牌列表
     * @param listPlayer2 玩家2卡牌列表
     * @param buffName Buff名称
     */
    private void BuffAction_war_next(PlayerAgainst playerAgainst1, PlayerAgainst playerAgainst2,
                                     List<CardBattle>listPlayer1,List<CardBattle>listPlayer2,
                                     String buffName) {
        playerAgainst1.getPlayerCondition().setHasWar(true);
    }

    /**
     * 竹判定Buff：若本回合未使用战斗或防守类卡牌，回合开始时抽3张牌并破坏对手1点护盾。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_bamboo_judge(PlayerAgainst user, PlayerAgainst target) {
        if(user.getPlayerCondition().isUsedBattleCard())return;
        if(user.getPlayerCondition().isUsedDefenseCard())return;
        AddStatus(user,List.of(new Status("bamboo",1,"回合开始时抽3张牌并破坏对手1点护盾")));
    }

    /**
     * 壮志难酬Buff（下回合）：设置壮志难酬Buff标记。
     * @param playerAgainst1 玩家1
     * @param playerAgainst2 玩家2
     * @param listPlayer1 玩家1卡牌列表
     * @param listPlayer2 玩家2卡牌列表
     * @param buffName Buff名称
     */
    private void BuffAction_zhuangzhinanchou_next(PlayerAgainst playerAgainst1, PlayerAgainst playerAgainst2,
                                                  List<CardBattle>listPlayer1,List<CardBattle>listPlayer2,
                                                  String buffName) {
        playerAgainst1.getPlayerCondition().setHasZhuangZhiNanChou(true);
    }

    /**
     * 淡泊判定Buff：本回合没使用战斗类卡牌，恢复3点血量且护盾上限+1，下回合战斗类牌面值减2。
     * @param user 施加Buff的玩家
     * @param target 目标玩家
     */
    private void BuffAction_danbo_judge(PlayerAgainst user, PlayerAgainst target){
        //本回合没使用战斗类卡牌，恢复3点血量且护盾上限+1，但是下回合战斗类牌面值减2
        if(user.getPlayerCondition().isUsedBattleCard())return;
        AddHP(user,3);
        if(user.getShield() == user.getShieldMax())
        {
            user.setShieldMax(user.getShieldMax()+1);
        }
        user.setShield(user.getShield()+1);
        user.getStatusesBegin().add(new Status("danbo",1,"攻击力-2"));
    }


    /**
     * 卡牌动作映射表，存储卡牌名称与对应处理方法的映射关系
     */
    private final Map<String, CardAction> cardActions = new HashMap<>();
    /**
     * 初始化卡牌动作映射表，将所有卡牌名称与对应的处理方法关联
     */
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


    /**
     * 春卡效果：恢复2点血量，若本回合未受伤害则添加判定状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Spring(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //实现效果：恢复1点血量。若本回合未受伤害，下回合获得3金币，且抽1张牌。
        //添加的spring_judge:
        //如果本回合没受到攻击，那么添加spring状态到statusBegin
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);
        AddHP(user,2);

        AddStatus(user, new ArrayList<Status>(List.of(new Status("spring_judge",1))));
    }

    /**
     * 火卡效果：造成1点伤害，若对方无护盾则添加火焰状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Fire(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成1点伤害。
        // 若对方无护盾，下回合使用者抽1张牌并使其下回合战斗类卡牌费用+1。
        user.getPlayerCondition().setUsedBattleCard(true);
        if(target.getShield() == 0 && !target.getPlayerCondition().isPurification())
        {
            AddStatus(user, new ArrayList<Status>(List.of(new Status("fire",1))));
        }
        AddShield(target,-(max(0,1+user.getPlayerCondition().getFireAdd())));
    }

    /**
     * 鸟卡效果：造成1点伤害，添加鸟类判定状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Bird(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成1点伤害。若本回合对面使用防守类卡，追加1点真实伤害。
        user.getPlayerCondition().setUsedBattleCard(true);

        AddShield(target,-(max(0,1+user.getPlayerCondition().getFireAdd())));
        if(!target.getPlayerCondition().isPurification())
            AddStatus(target, new ArrayList<Status>(List.of(new Status("bird_judge",1))));
    }

    /**
     * 秋卡效果：对手金币-2，添加秋季判定状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Autumn(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //对手金币-2。若其本回合未获得新护盾，弃1张牌并失去1点护盾。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        AddCoins(target,-2);
        if(!target.getPlayerCondition().isPurification())
            AddStatus(target, new ArrayList<Status>(List.of(new Status("autumn_judge",1))));

    }

    /**
     * 山卡效果：获得1点护盾，添加山峰判定状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Mountain(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //获得1点护盾。若本回合未受伤害，下三回合各+1护盾。
        user.getPlayerCondition().setUsedDefenseCard(true);

        AddShield(user,1/user.getPlayerCondition().getSunBuff());
        AddStatus(user, new ArrayList<Status>(List.of(new Status("mountain_judge",1))));

    }

    /**
     * 水卡效果：获得1点护盾，添加水流判定状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Water(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //获得1点护盾。若未使用战斗类卡牌，恢复2点血量。
        user.getPlayerCondition().setUsedDefenseCard(true);

        AddShield(user,1/user.getPlayerCondition().getSunBuff());
        AddStatus(target, new ArrayList<Status>(List.of(new Status("water_judge",1))));

    }

    /**
     * 月卡效果：令对手失去1点护盾并随机弃1张牌
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Moon(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //令对手失去1点护盾并随机弃1张牌。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        killShield(target,-1);
        cardService.RandomDiscardCardsList(target.getCards(),1);
    }

    /**
     * 悲伤卡效果：令对手弃1张手牌，根据手牌数量产生不同效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Sad(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //令对手弃1张手牌。
        // 若手牌少于3张，失去3点护盾且下一回合无法获得护盾。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        cardService.RandomDiscardCardsList(target.getCards(),1);
        if(target.getPlayerCondition().isPurification())return;
        if(target.countCardsNum()<3)
        {
            killShield(target,-3);
            AddStatus(target, new ArrayList<Status>(List.of(new Status("sad",1))));
        }
        else if(target.countCardsNum()>=5)
            AddCoins(target,1);
    }

    /**
     * 家卡效果：获得2点护盾，若护盾满则产生额外效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Home(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //获得2点护盾。若此时护盾满了，恢复3点血量且下回合抽1张牌。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        AddShield(user,2);
        if(user.getShield() == user.getShieldMax())
        {
            AddHP(user,3);
            AddStatus(user,new ArrayList<Status>(List.of(new Status("home",1))));
        }

    }

    /**
     * 酒卡效果：造成2点伤害，添加酒类判定状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Wine(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成2点伤害。
        // 若本回合受到攻击，抽2张牌且下回合战斗伤害+1。
        user.getPlayerCondition().setUsedBattleCard(true);
        AddShield(target,-(max(0,2+user.getPlayerCondition().getFireAdd())));

        AddStatus(user,new ArrayList<>(List.of(new Status("wine_judge",1))));
    }

    /**
     * 柳卡效果：获得2点护盾，添加柳树判定状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Liu(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //获得2点护盾。
        // 若本回合受到攻击，恢复3点护盾并免疫下回合1点伤害。
        user.getPlayerCondition().setUsedDefenseCard(true);

        AddShield(target,2/user.getPlayerCondition().getSunBuff());
        AddStatus(user,new ArrayList<>(List.of(new Status("liu_judge",1))));

    }

    /**
     * 日卡效果：造成2点伤害，若对方有护盾则额外破坏护盾
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Sun(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成2点伤害。
        // 伤害前，若对方有护盾，额外破坏2点护盾并使其下回合防守效果减半。
        user.getPlayerCondition().setUsedBattleCard(true);

        if(target.getShield()>0 && !target.getPlayerCondition().isPurification())
        {
            killShield(target,-2);
            AddStatus(target, new ArrayList<Status>(List.of(new Status("sun",1,"下回合防守卡牌增益减半"))));

        }
        AddShield(target,-(max(0,2+user.getPlayerCondition().getFireAdd())));
    }

    /**
     * 雁卡效果：获得2点护盾，添加大雁状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Goose(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //获得2点护盾。下两回合受到伤害减少1点，若护盾被破则反弹1点真实伤害,同时移除本buff。
        user.getPlayerCondition().setUsedDefenseCard(true);

        AddShield(user,2/user.getPlayerCondition().getSunBuff());
        AddStatus(user,new ArrayList<>(List.of(new Status("goose",2))));

    }

    /**
     * 友情卡效果：随机获得1张1-2费牌，根据手牌数量产生不同效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Friend(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //随机获得1张1-2费牌。若手牌少于3张，再抽2张1-3费牌，但下回合攻击卡牌面值-1。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        List<CardBattle>added= cardService.RandomGetCardsByNumAndCost(1,2);
        List<CardBattle>newCards=cardService.MergeCardList(added,user.getCards());
        user.setCards(newCards);
        if(user.countCardsNum()<3){
            added= cardService.RandomGetCardsByNumAndCost(2,3);
            newCards=cardService.MergeCardList(added,user.getCards());
            user.setCards(newCards);
            AddStatus(user,new ArrayList<>(List.of(new Status("friend",1))));
        }

    }

    /**
     * 雨卡效果：造成2点伤害，添加雨水状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Rain(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成2点伤害。若对方下回合使用防守卡，该卡无效且追加3点伤害。
        user.getPlayerCondition().setUsedBattleCard(true);
        AddShield(target,-(max(0,2+user.getPlayerCondition().getFireAdd())));
        if(target.getPlayerCondition().isPurification())return;

        AddStatus(target,new ArrayList<>(List.of(new Status("rain_next",1))));
    }

    /**
     * 战争卡效果：造成2点伤害，添加战争状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_War(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成2点伤害。若对方下回合使用了进攻，再造成3点真实伤害。
        user.getPlayerCondition().setUsedBattleCard(true);
        AddShield(target,-(max(0,2+user.getPlayerCondition().getFireAdd())));
        if(target.getPlayerCondition().isPurification())return;

        AddStatus(target,new ArrayList<>(List.of(new Status("war_next",1))));
    }

    /**
     * 自然卡效果：根据护盾数量产生不同效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Nature(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        // 若护盾≥3，额外获得2点护盾。
        // 否则下回合免疫一次破盾的额外伤害
        //
        user.getPlayerCondition().setUsedDefenseCard(true);

        AddHP(user,2/user.getPlayerCondition().getSunBuff());
        if(user.getShield()>=3)
        {
            AddShield(user,3/user.getPlayerCondition().getSunBuff());
        }
        else {
            AddStatus(user,new ArrayList<>(List.of(new Status("nature",1))));
        }
    }

    /**
     * 离别卡效果：令对手弃2张手牌，根据护盾数量产生不同效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Byebye(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //令对手弃2张手牌。若对手护盾小于等于5，造成4点真实伤害且下两回合获得金币数量减半。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        cardService.RandomDiscardCardsList(target.getCards(),2);
        if(target.getShield()<=3 &&
                !target.getPlayerCondition().isPurification())
        {
            AddHP(target,-4);
            AddStatus(target,new ArrayList<>(List.of(new Status("byebye",2))));
        }
    }

    /**
     * 桃花卡效果：恢复3点血量，添加桃花状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Flower(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //恢复3点血量，下三回合各获得2点护盾且每回合回1点血。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        AddHP(user,3);
        AddStatus(user,new ArrayList<>(List.of(new Status("flower",3))));
    }

    /**
     * 竹卡效果：造成3点真实伤害，添加竹子状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Bamboo(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成3点真实伤害。若未使用其他卡，抽3张牌并破坏对手1点护盾。
        user.getPlayerCondition().setUsedBattleCard(true);
        AddShield(target,-(max(0,3+user.getPlayerCondition().getFireAdd())));
        AddStatus(user,new ArrayList<>(List.of(new Status("bamboo_judge",3))));
    }

    /**
     * 壮志难酬卡效果：添加壮志难酬状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Zhuangzhinanchou(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //对手本回合无法获得护盾。若其下回合获得一定量护盾，则同时给己方添加等量护盾。
        user.getPlayerCondition().setUsedDefenseCard(true);
        target.getPlayerCondition().setCanAddShield(false);
        if(target.getPlayerCondition().isPurification())return;

        AddStatus(target,new ArrayList<>(List.of(new Status("zhuangzhinanchou_next",1))));
    }

    /**
     * 淡泊卡效果：获得4点护盾，根据护盾数量产生不同效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Danbo(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //获得4点护盾。若使用者护盾≥5且本回合没使用战斗类卡牌，恢复3点血量且护盾上限+1，但是下回合战斗类牌面值减半（向上取整）。
        user.getPlayerCondition().setUsedDefenseCard(true);

        if(user.getShield()>=5)
            AddStatus(user,new ArrayList<>(List.of(new Status("danbo_judge",1))));
        AddShield(user,4/user.getPlayerCondition().getSunBuff());

    }

    /**
     * 黄河卡效果：造成4点真实伤害，根据护盾数量产生不同效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Yellowriver(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成4点真实伤害。若对方护盾≥5，摧毁所有护盾，同时使目标每回合回血量-2。
        user.getPlayerCondition().setUsedBattleCard(true);
        if(user.getPlayerCondition().getFireAdd()>0) {
            AddShield(target, -(user.getPlayerCondition().getFireAdd()));
            AddHP(target, -4);
            //        AddShield(target,-(max(0,1+user.getPlayerCondition().getFireAdd())));
        }
        else
            AddHP(target,-(max(0,4+user.getPlayerCondition().getFireAdd())));
        if(target.getShield()>=5)
        {
            target.setShield(0);
            AddStatus(target,new ArrayList<>(List.of(new Status("yellowriver",1))));

        }
    }

    /**
     * 思念卡效果：造成5点伤害，根据血量产生不同效果
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Missing(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //造成5点伤害。若对方血量≤10，追加5点真实伤害且无视免疫效果。
        user.getPlayerCondition().setUsedBattleCard(true);
        AddShield(target,-(max(0,5+user.getPlayerCondition().getFireAdd())));
        if(target.getHp()<=5)AddHP(target,-3);
    }

    /**
     * 长江卡效果：获得5点护盾，增加护盾上限，添加长江状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Longriver(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
        //获得5点护盾。护盾上限+3。下三回合每回合恢复4点血量。
        user.getPlayerCondition().setUsedDefenseCard(true);

        AddShield(user,5/user.getPlayerCondition().getSunBuff());
        user.setShieldMax(user.getShieldMax()+3);
        user.setShield(user.getShield()+3);
        AddStatus(target,new ArrayList<>(List.of(new Status("longriver",3))));


    }

    /**
     * 爱情卡效果：恢复5点血量，失去2点金币，添加爱情状态
     * @param user 使用者
     * @param target 目标玩家
     * @param cardBattle 使用的卡牌
     */
    private void Action_Love(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle){
        //恢复5点血量，失去2点金币，下2回合各获得4点护盾且免疫所有减益，同时每回合开始时减少3枚金币。
        user.getPlayerCondition().setUsedProfitOrDecreaseCard(true);

        AddHP(user,5);
        AddCoins(user,-4);
        AddStatus(target,new ArrayList<>(List.of(new Status("love",2))));
    }

    //几个原子操作

    /**
     * 卡牌服务实例，用于处理卡牌相关操作
     */
    CardService cardService=new CardService();

    /**
     * 为玩家增加或减少金币
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
     * @param coins 要增加的金币数量，正数表示增加，负数表示减少
     */
    public void AddCoins(PlayerAgainst playerAgainst , int coins) {
        //修改player的金币
        playerAgainst.setWealthy(playerAgainst.getWealthy() + coins);
        if(playerAgainst.getWealthy()<0)playerAgainst.setWealthy(0);
    }

    /**
     * 为玩家增加或减少血量
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
     * @param hp 要增加的血量，正数表示恢复血量，负数表示扣除血量
     */
    public void AddHP(PlayerAgainst playerAgainst , int hp) {
        //修改player的血量
        playerAgainst.setHp(playerAgainst.getHp() + hp);
        if(hp>0)
            playerAgainst.getPlayerCondition().setCured(true);
        else if(hp<0)
            playerAgainst.getPlayerCondition().setRobbedHP(true);
        if(playerAgainst.getShield()==0 &&
                playerAgainst.getPlayerCondition().isLiuBuff() &&
                hp<0)
            playerAgainst.setHp(playerAgainst.getHp()+1);
        if(playerAgainst.getHp()>playerAgainst.getHpMax())
        {
            AddCoins(playerAgainst,2*(playerAgainst.getHp()-playerAgainst.getHpMax()));
            playerAgainst.setHp(playerAgainst.getHpMax());
        }
    }

    /**
     * 为玩家增加或减少护盾，改成为玩家造成的伤害
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
     * @param shield 要增加的护盾数量，正数表示增加护盾，负数表示减少护盾
     */
    public void AddShield(PlayerAgainst playerAgainst , int shield) {
        //修改player的护盾

        if(shield>0) {
            if(playerAgainst.getPlayerCondition().isCanAddShield())
            {
                playerAgainst.getPlayerCondition().setAddedShield(true);
                playerAgainst.getPlayerCondition().setNumOfShieldAdd(playerAgainst.getPlayerCondition().getNumOfShieldAdd()+shield);
            }
            else return;
        }
        else if (shield<0)
            playerAgainst.getPlayerCondition().setAttacked(true);
        playerAgainst.setShield(playerAgainst.getShield() + shield);
        if(playerAgainst.getShield()<0)
        {
            if(!playerAgainst.getPlayerCondition().isNatureBuff())
                AddHP(playerAgainst,playerAgainst.getShield());
            playerAgainst.setShield(0);
        }
        else if(playerAgainst.getShield() >playerAgainst.getShieldMax())
        {
            AddCoins(playerAgainst,playerAgainst.getShield()-playerAgainst.getShieldMax());
            playerAgainst.setShield(playerAgainst.getShieldMax());
        }
    }

    /**
     * 直接破坏护盾，不会对血量造成影响
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
     * @param shield 要破坏的护盾数量，应为负数
     */
    public void killShield(PlayerAgainst playerAgainst , int shield){
        playerAgainst.setShield(playerAgainst.getShield()+shield);
        playerAgainst.setShield(max(playerAgainst.getShield(),0));
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
    public void AddPlayerCards(PlayerAgainst playerAgainst , int addNum, int costs) {
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
    public void DiscardPlayerCards(PlayerAgainst playerAgainst , int num) {
        //给用户随机删除num张卡牌
        playerAgainst.setCards(cardService.RandomDiscardCardsList(playerAgainst.getCards(), num));
    }

    /**
     * 为玩家添加回合开始时的状态效果
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的状态列表）
     * @param statuses 要添加的状态列表，如果为null或空列表则不添加任何状态
     */
    public void AddBeginStatus(PlayerAgainst playerAgainst , List<Status> statuses){
        // 空值检查
        if (statuses == null || statuses.isEmpty()) {
            return;
        }
        // 确保玩家的状态列表不为null
        if (playerAgainst.getStatusesBegin() == null) {
            playerAgainst.setStatusesBegin(new ArrayList<>());
        }

        playerAgainst.getStatusesBegin().addAll(statuses);
    }

    /**
     * 为玩家添加回合结束时的状态效果
     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的状态列表）
     * @param statuses 要添加的状态列表，如果为null或空列表则不添加任何状态
     */
    public void AddStatus(PlayerAgainst playerAgainst , List<Status> statuses) {
        // 空值检查
        if (statuses == null || statuses.isEmpty()) {
            return;
        }

        // 确保玩家的状态列表不为null
        if (playerAgainst.getStatusesEnd() == null) {
            playerAgainst.setStatusesEnd(new ArrayList<>());
        }

        // 添加所有状态
        playerAgainst.getStatusesEnd().addAll(statuses);
    }

    /**
     * 计算和处理玩家的状态效果
     * @param playerAgainst 要处理状态的玩家对象
     */
    public void CalStatus(PlayerAgainst playerAgainst ){}
}




//package com.example.bg.GameBG.Play.Service;
//
//import com.example.bg.GameBG.Play.Entities.CardBattle;
//import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
//import com.example.bg.GameBG.Play.Entities.Status;
//import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
//import org.springframework.cglib.transform.impl.AddStaticInitTransformer;
//
//import java.util.*;
//
//import static java.lang.Math.max;
// * 卡牌动作接口，定义卡牌效果的执行方法
// */
//interface CardAction{
//    /**
//     * 执行卡牌效果
//     * @param user 使用卡牌的玩家
//     * @param target 目标玩家
//     * @param card 使用的卡牌
//     */
//    void execute(PlayerAgainst user,PlayerAgainst target, CardBattle card);
//}
// * 玩家服务类，负责处理游戏中的各种对战结算功能
// * 主要包含卡牌效果处理、状态管理、玩家属性修改等核心功能
// */
//public class PlayerService {
//    /*
//    * 这里进行各种对战结算函数
//    * 分成总MainService，里面用switch切换到各种，最后返回user
//    * 还有一个关键点就是：这里只负责处理
//    * 对于状态：所有状态一律加到statusesEnd中，在结束的Main里处理End为begin，begin才是真正有用的buff
//    *
//    * */
//    /**
//     * 构造函数，初始化卡牌动作映射
//     */
//    public PlayerService(){
//        initializeCardActions();
//    }
//    //补充：
//    //合成卡牌专用函数
//    public void SynthesizeABC(PlayerAgainst playerAgainst,String a,String b,String c){
//        playerAgainst.setCards(cardService.DiscardCard(playerAgainst.getCards(),a));
//        playerAgainst.setCards(cardService.DiscardCard(playerAgainst.getCards(),b));
//        playerAgainst.setCards(cardService.GetCardByName(playerAgainst.getCards(),c));
//
//    }
//    //弃卡专用函数
//    public void DiscardPlayerCards(PlayerAgainst playerAgainst,List<CardBattle>list){
//
//    }
//
//
//    //一个顶端流程函数
//    //三个流程实现函数
//
//    public void MainService(PlayerAgainst playerAgainst1,PlayerAgainst playerAgainst2,
//                            List<CardBattle>listPlayer1,List<CardBattle>listPlayer2){
//        //接受的是本回合出牌列表
//
//
//    }
//    /**
//     * 主服务方法：根据卡牌名称执行对应的动作
//     * @param user 使用者
//     * @param target 目标玩家
//     * @param cardBattle 使用的卡牌
//     */
//    public void MainOpService(PlayerAgainst user,PlayerAgainst target,CardBattle cardBattle)
//    {
//        String cardName = cardBattle.getCardName();
//        CardAction action = cardActions.get(cardName);
//
//        if (action != null) {
//            action.execute(user,target, cardBattle);
//        } else {
//            // 处理未知卡牌
//            System.out.println("未知卡牌: " + cardName);
//        }
//
//    }
//
//    public void EndService(PlayerAgainst user)
//    {
//
//    }
//
//    private void BeginService(PlayerAgainst user)
//    {
//
//    }
//
//
//
//    //这一部分是卡牌效果
//    //这个的设计思路就是只传受影响的玩家和影响他的卡牌
//    private final Map<String, CardAction> cardActions = new HashMap<>();
//
//
//
//    private void initializeCardActions() {
//        cardActions.put("spring", this::Action_Spring);
//        cardActions.put("fire", this::Action_Fire);
//        cardActions.put("bird", this::Action_Bird);
//        cardActions.put("autumn", this::Action_Autumn);
//        cardActions.put("mountain", this::Action_Mountain);
//        cardActions.put("water", this::Action_Water);
//        cardActions.put("moon", this::Action_Moon);
//        cardActions.put("sad", this::Action_Sad);
//        cardActions.put("home", this::Action_Home);
//        cardActions.put("wine", this::Action_Wine);
//        cardActions.put("liu", this::Action_Liu);
//        cardActions.put("sun", this::Action_Sun);
//        cardActions.put("goose", this::Action_Goose);
//        cardActions.put("friend", this::Action_Friend);
//        cardActions.put("rain", this::Action_Rain);
//        cardActions.put("war", this::Action_War);
//        cardActions.put("nature", this::Action_Nature);
//        cardActions.put("byebye", this::Action_Byebye);
//        cardActions.put("flower", this::Action_Flower);
//        cardActions.put("bamboo", this::Action_Bamboo);
//        cardActions.put("zhuangzhinanchou", this::Action_Zhuangzhinanchou);
//        cardActions.put("danbo", this::Action_Danbo);
//        cardActions.put("yellowriver", this::Action_Yellowriver);
//        cardActions.put("missing", this::Action_Missing);
//        cardActions.put("longriver", this::Action_Longriver);
//        cardActions.put("love", this::Action_Love);
//    }
//
//    private void Action_Spring(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //实现效果：恢复1点血量。若本回合未受伤害，下回合获得3金币，且抽1张牌。
//        //添加的spring_judge:
//        //如果本回合没受到攻击，那么添加spring状态到statusBegin
//        AddHP(user,2);
//        AddStatus(user, new ArrayList<Status>(List.of(new Status("spring_judge",1))));
//    }
//    private void Action_Fire(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成1点伤害。
//        // 若对方无护盾，抽1张牌并使其下回合战斗类卡牌费用+1。
//        if(target.getShield() == 0)
//        {
//            AddStatus(user, new ArrayList<Status>(List.of(new Status("fire",1))));
//        }
//        AddShield(target,-1);
//    }
//    private void Action_Bird(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成1点伤害。若本回合对面使用防守类卡，追加1点真实伤害。
//        AddShield(target,-1);
//        AddStatus(target, new ArrayList<Status>(List.of(new Status("bird_judge",1))));
//    }
//    private void Action_Autumn(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //对手金币-2。若其本回合未获得新护盾，弃1张牌并失去1点护盾。
//        AddCoins(target,-2);
//        AddStatus(target, new ArrayList<Status>(List.of(new Status("autumn_judge",1))));
//
//    }
//    private void Action_Mountain(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //获得1点护盾。若本回合未受伤害，下三回合各+1护盾。
//        AddShield(user,1);
//        AddStatus(user, new ArrayList<Status>(List.of(new Status("mountain_judge",1))));
//
//    }
//    private void Action_Water(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //获得1点护盾。若未使用战斗类卡牌，恢复2点血量。
//        AddShield(user,1);
//        AddStatus(target, new ArrayList<Status>(List.of(new Status("water_judge",1))));
//
//    }
//    private void Action_Moon(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //令对手失去1点护盾并随机弃1张牌。
//        killShield(target,-1);
//        cardService.RandomDiscardCardsList(target.getCards(),1);
//    }
//    private void Action_Sad(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //令对手弃1张手牌。
//        // 若手牌少于3张，失去3点护盾且下一回合无法获得护盾。
//        cardService.RandomDiscardCardsList(target.getCards(),1);
//        if(target.countCardsNum()<3)
//        {
//            killShield(target,-3);
//            AddStatus(target, new ArrayList<Status>(List.of(new Status("sad",1))));
//        }
//        else if(target.countCardsNum()>=5)
//            AddCoins(target,1);
//    }
//    private void Action_Home(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //获得2点护盾。若此时护盾满了，恢复3点血量且下回合抽1张牌。
//        AddShield(user,2);
//        if(user.getShield() == user.getShieldMax())
//        {
//            AddHP(user,3);
//            AddStatus(user,new ArrayList<Status>(List.of(new Status("home",1))));
//        }
//
//    }
//    private void Action_Wine(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成2点伤害。
//        // 若本回合受到攻击，抽2张牌且下回合战斗伤害+1。
//        AddShield(target,-2);
//        AddStatus(user,new ArrayList<>(List.of(new Status("wine_judge",1))));
//    }
//    private void Action_Liu(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //获得2点护盾。
//        // 若本回合受到攻击，恢复3点护盾并免疫下回合1点伤害。
//        AddShield(target,2);
//        AddStatus(user,new ArrayList<>(List.of(new Status("liu_judge",1))));
//
//    }
//    private void Action_Sun(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成2点伤害。伤害前，若对方有护盾，额外破坏2点护盾并使其下回合防守面值减半（向下取整）。
//        if(target.getShield()>0)
//        {
//            killShield(target,-2);
//            AddStatus(target, new ArrayList<Status>(List.of(new Status("sun_judge",1))));
//
//        }
//        AddShield(target,-2);
//    }
//    private void Action_Goose(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //获得2点护盾。下两回合受到伤害减少1点，若护盾被破则反弹1点真实伤害,同时移除本buff。
//        AddShield(user,2);
//        AddStatus(user,new ArrayList<>(List.of(new Status("goose",2))));
//
//    }
//    private void Action_Friend(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //随机获得1张1-2费牌。若手牌少于3张，再抽2张1-3费牌，但下回合攻击卡牌面值-1。
//        List<CardBattle>added= cardService.RandomGetCardsByNumAndCost(1,2);
//        List<CardBattle>newCards=cardService.MergeCardList(added,user.getCards());
//        user.setCards(newCards);
//        if(user.countCardsNum()<3){
//            added= cardService.RandomGetCardsByNumAndCost(2,3);
//            newCards=cardService.MergeCardList(added,user.getCards());
//            user.setCards(newCards);
//            AddStatus(user,new ArrayList<>(List.of(new Status("friend",1))));
//        }
//
//    }
//    private void Action_Rain(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成2点伤害。若对方下回合使用防守卡，该卡无效且追加3点伤害。
//        AddShield(target,-2);
//        AddStatus(target,new ArrayList<>(List.of(new Status("rain",1))));
//    }
//    private void Action_War(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成2点伤害。若对方下回合使用了进攻，再造成3点真实伤害。
//        AddShield(target,-2);
//        AddStatus(target,new ArrayList<>(List.of(new Status("war",1))));
//    }
//    private void Action_Nature(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        // 若护盾≥3，额外获得2点护盾。
//        // 否则下回合免疫一次破盾的额外伤害
//        // 获得2点护盾并恢复1点血量。
//        if(user.getShield()>=3)
//        {
//            AddShield(user,4);
//            AddHP(user,1);
//        }
//        else {
//            AddShield(user,2);
//            AddHP(user,1);
//            AddStatus(user,new ArrayList<>(List.of(new Status("nature",1))));
//        }
//    }
//    private void Action_Byebye(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //令对手弃2张手牌。若对手护盾小于等于5，造成4点真实伤害且下两回合获得金币数量减半。
//        cardService.RandomDiscardCardsList(target.getCards(),2);
//        if(target.getShield()<=5)
//        {
//            AddHP(target,-4);
//            AddStatus(target,new ArrayList<>(List.of(new Status("byebye",1))));
//        }
//    }
//    private void Action_Flower(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //恢复3点血量，下三回合各获得2点护盾且每回合回1点血。
//        AddHP(user,3);
//        AddStatus(user,new ArrayList<>(List.of(new Status("flower",3))));
//    }
//    private void Action_Bamboo(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成3点真实伤害。若未使用其他卡，抽3张牌并破坏对手1点护盾。
//        AddHP(target,-3);
//        AddStatus(user,new ArrayList<>(List.of(new Status("bamboo",3))));
//    }
//    private void Action_Zhuangzhinanchou(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //对手本回合无法获得护盾。若其下回合获得一定量护盾，则同时给己方添加等量护盾。
//        AddStatus(user,new ArrayList<>(List.of(new Status("zhuangzhinanchou",1))));
//    }
//    private void Action_Danbo(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //获得4点护盾。若使用者护盾≥5且本回合没使用战斗类卡牌，恢复3点血量且护盾上限+1，但是下回合战斗类牌面值减半（向上取整）。
//        if(user.getShield()>=5)
//            AddStatus(user,new ArrayList<>(List.of(new Status("danbo",1))));
//        AddShield(user,4);
//
//    }
//    private void Action_Yellowriver(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成4点真实伤害。若对方护盾≥5，摧毁所有护盾，同时使目标每回合回血量-2。
//        AddHP(target,-4);
//        if(target.getShield()>=5)
//        {
//            target.setShield(0);
//            AddStatus(target,new ArrayList<>(List.of(new Status("yellowriver",1))));
//
//        }
//    }
//    private void Action_Missing(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //造成5点伤害。若对方血量≤10，追加5点真实伤害且无视免疫效果。
//        AddShield(target,-5);
//        if(target.getHp()<=5)AddHP(target,-5);
//    }
//    private void Action_Longriver(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle) {
//        //获得5点护盾。护盾上限+3。下三回合每回合恢复4点血量。
//        AddShield(user,5);
//        user.setShieldMax(user.getShieldMax()+3);
//        AddStatus(target,new ArrayList<>(List.of(new Status("longriver",1))));
//
//
//    }
//    private void Action_Love(PlayerAgainst user,PlayerAgainst target, CardBattle cardBattle){
//        //恢复5点血量，失去2点金币，下2回合各获得4点护盾且免疫所有减益，同时每回合开始时减少3枚金币。
//        AddHP(user,5);
//        AddCoins(user,-2);
//        AddStatus(target,new ArrayList<>(List.of(new Status("love",1))));
//    }
//
//
//
//
//
//
//
//    //几个原子操作
//    CardService cardService=new CardService();
//    /**
//     * 为玩家增加或减少金币
//     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
//     * @param coins 要增加的金币数量，正数表示增加，负数表示减少
//     */
//    public void AddCoins(PlayerAgainst playerAgainst , int coins) {
//        //修改player的金币
//        playerAgainst.setWealthy(playerAgainst.getWealthy() + coins);
//        if(playerAgainst.getWealthy()<0)playerAgainst.setWealthy(0);
//    }
//
//    /**
//     * 为玩家增加或减少血量
//     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
//     * @param hp 要增加的血量，正数表示恢复血量，负数表示扣除血量
//     */
//    public void AddHP(PlayerAgainst playerAgainst , int hp) {
//        //修改player的血量
//        playerAgainst.setHp(playerAgainst.getHp() + hp);
//        if(playerAgainst.getHp()>playerAgainst.getHpMax())
//        {
//            AddCoins(playerAgainst,2*(playerAgainst.getHp()-playerAgainst.getHpMax()));
//            playerAgainst.setHp(playerAgainst.getHpMax());
//        }
//    }
//
//    /**
//     * 为玩家增加或减少护盾
//     * 改成为玩家造成的伤害
//     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入的对象）
//     * @param shield 要增加的护盾数量，正数表示增加护盾，负数表示减少护盾
//     */
//    public void AddShield(PlayerAgainst playerAgainst , int shield) {
//        //修改player的护盾
//        playerAgainst.setShield(playerAgainst.getShield() + shield);
//
//        if(playerAgainst.getShield()<0)
//        {
//            AddHP(playerAgainst,playerAgainst.getShield());
//            playerAgainst.setShield(0);
//        }
//        else if(playerAgainst.getShield() >playerAgainst.getShieldMax())
//        {
//            AddCoins(playerAgainst,playerAgainst.getShield()-playerAgainst.getShieldMax());
//            playerAgainst.setShield(playerAgainst.getShieldMax());
//        }
//    }
//
//    public void killShield(PlayerAgainst playerAgainst , int shield){
//        playerAgainst.setShield(playerAgainst.getShield()+shield);
//        playerAgainst.setShield(max(playerAgainst.getShield(),0));
//    }
//    /**
//     * 修改卡牌的面值大小
//     * @param cardBattle 要修改的卡牌对象（注意：此方法会直接修改传入的对象）
//     * @param delta 面值变化量，正数表示增加面值，负数表示减少面值
//     */
//    public void ModifyCardSize(CardBattle cardBattle, int delta) {
//        //修改卡牌的牌面大小
//        cardBattle.setCardSize(cardBattle.getCardSize() + delta);
//    }
//
//    /**
//     * 为玩家添加随机卡牌
//     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的卡牌列表）
//     * @param addNum 要添加的卡牌数量，如果小于等于0则不添加任何卡牌
//     * @param costs 卡牌面值上限，如果为-1则不限制面值
//     */
//    public void AddPlayerCards(PlayerAgainst playerAgainst , int addNum, int costs) {
//        //给用户添加卡牌
//        List<CardBattle> old = playerAgainst.getCards();
//        List<CardBattle> added = cardService.RandomGetCardsByNumAndCost(addNum, costs);
//        playerAgainst.setCards(cardService.MergeCardList(old, added));
//    }
//
//    /**
//     * 随机丢弃玩家的卡牌
//     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的卡牌列表）
//     * @param num 要丢弃的卡牌数量，如果小于等于0则不丢弃任何卡牌，如果大于玩家拥有的卡牌总数则丢弃全部卡牌
//     */
//    public void DiscardPlayerCards(PlayerAgainst playerAgainst , int num) {
//        //给用户随机删除num张卡牌
//        playerAgainst.setCards(cardService.RandomDiscardCardsList(playerAgainst.getCards(), num));
//    }
//
//    public void AddBeginStatus(PlayerAgainst playerAgainst , List<Status> statuses){
//        // 空值检查
//        if (statuses == null || statuses.isEmpty()) {
//            return;
//        }
//        // 确保玩家的状态列表不为null
//        if (playerAgainst.getStatusesBegin() == null) {
//            playerAgainst.setStatusesBegin(new ArrayList<>());
//        }
//
//        playerAgainst.getStatusesBegin().addAll(statuses);
//    }
//
//    /**
//     * 为玩家添加状态效果
//     * @param playerAgainst 目标玩家对象（注意：此方法会直接修改传入对象的状态列表）
//     * @param statuses 要添加的状态列表，如果为null或空列表则不添加任何状态
//     */
//    public void AddStatus(PlayerAgainst playerAgainst , List<Status> statuses) {
//        // 空值检查
//        if (statuses == null || statuses.isEmpty()) {
//            return;
//        }
//
//        // 确保玩家的状态列表不为null
//        if (playerAgainst.getStatusesEnd() == null) {
//            playerAgainst.setStatusesEnd(new ArrayList<>());
//        }
//
//        // 添加所有状态
//        playerAgainst.getStatusesEnd().addAll(statuses);
//    }
//
//    //结算status
//    public void CalStatus(PlayerAgainst playerAgainst ){}
//}

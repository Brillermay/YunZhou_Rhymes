package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.CardBattle;

import java.util.*;

public class CardService {
    /*
    * 处理所有和卡牌相关的函数
    * 查找卡牌
    * 合并卡牌列表
    * 随机获得x费卡
    *
    * */
    private static final List<CardBattle> arrayList = new ArrayList<>();
    static {
        arrayList.add(new CardBattle("profit", 1, "spring", 1));      // 春
        arrayList.add(new CardBattle("battle", 1, "fire", 1));        // 火
        arrayList.add(new CardBattle("battle", 1, "bird", 1));        // 鸟
        arrayList.add(new CardBattle("decrease", 1, "autumn", 1));    // 秋
        arrayList.add(new CardBattle("defense", 1, "mountain", 1));   // 山
        arrayList.add(new CardBattle("defense", 1, "water", 1));      // 水
        arrayList.add(new CardBattle("decrease", 1, "moon", 1));      // 月
        arrayList.add(new CardBattle("decrease", 1, "sad", 2));       // 悲
        arrayList.add(new CardBattle("profit", 1, "home", 2));        // 故乡
        arrayList.add(new CardBattle("battle", 1, "wine", 2));        // 酒
        arrayList.add(new CardBattle("defense", 1, "liu", 2));     // 柳
        arrayList.add(new CardBattle("battle", 1, "sun", 2));         // 日
        arrayList.add(new CardBattle("defense", 1, "goose", 2));  // 雁
        arrayList.add(new CardBattle("profit", 1, "friend", 2));  // 友情
        arrayList.add(new CardBattle("battle", 1, "rain", 2));        // 雨
        arrayList.add(new CardBattle("battle", 1, "war", 2));         // 战争
        arrayList.add(new CardBattle("defense", 1, "nature", 2));     // 自然
        arrayList.add(new CardBattle("decrease", 1, "byebye", 3));   // 离别
        arrayList.add(new CardBattle("profit", 1, "flower", 3));// 桃花
        arrayList.add(new CardBattle("battle", 1, "bamboo", 3));      // 竹
        arrayList.add(new CardBattle("defense", 1, "zhuangzhinanchou", 3));// 壮志难酬
        arrayList.add(new CardBattle("defense", 1, "danbo", 4));      // 淡泊
        arrayList.add(new CardBattle("battle", 1, "yellowriver", 4)); // 黄河
        arrayList.add(new CardBattle("battle", 1, "missing", 5));     // 思念
        arrayList.add(new CardBattle("defense", 1, "longriver", 5));    // 长江
        arrayList.add(new CardBattle("profit", 1, "love", 7));        // 爱情
    }
    /**
     * 根据卡牌名称查找对应的卡牌
     * @param cardName 要查找的卡牌名称（不区分大小写）
     * @return 找到的卡牌对象，如果未找到则返回null
     */
    public static CardBattle getByName(String cardName) {
        //实现功能：根据名字查找卡牌
        for (CardBattle card : arrayList) {
            if (card.getCardName().equalsIgnoreCase(cardName)) {
                return card;
            }
        }

        return new CardBattle(); // 未找到返回空
    }
    /**
     * 合并两个卡牌列表，将相同名称的卡牌数量相加
     * @param list1 第一个卡牌列表，可以为null
     * @param list2 第二个卡牌列表，可以为null
     * @return 合并后的卡牌列表，只包含数量大于0的卡牌，如果两个列表都为null则返回空列表
     */
    public List<CardBattle> MergeCardList(List<CardBattle> list1, List<CardBattle> list2) {
        //实现功能：合并两个list
        //具体来说：对里面的每一种CardBattle，如果名字一样，那么就只放入一个名字，然后把cardNum相加，如果cardNum为0，那么就不加入

        Map<String, CardBattle> map = new HashMap<>();

        // 合并list1（处理单个list内的合并）
        mergeListIntoMap(list1, map);

        // 合并list2（处理两个list间的合并）
        mergeListIntoMap(list2, map);

        // 过滤cardNum为0或负数的卡牌，返回结果
        List<CardBattle> result = new ArrayList<>();
        for (CardBattle card : map.values()) {
            if (card.getCardNum() > 0) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * 将list中的卡牌合并到map中
     * @param list 要合并的卡牌列表
     * @param map 目标map
     */
    private void mergeListIntoMap(List<CardBattle> list, Map<String, CardBattle> map) {
        // 处理null list的情况
        if (list == null) {
            return;
        }

        for (CardBattle card : list) {
            // 跳过null元素
            if (card == null) {
                continue;
            }

            String cardName = card.getCardName();

            if (map.containsKey(cardName)) {
                // 如果map中已存在该卡牌，累加cardNum
                CardBattle existingCard = map.get(cardName);
                existingCard.setCardNum(existingCard.getCardNum() + card.getCardNum());
            } else {
                // 如果map中不存在该卡牌，创建新的CardBattle对象
                map.put(cardName, new CardBattle(
                        card.getCardType(),
                        card.getCardNum(),
                        card.getCardName(),
                        card.getCardSize()
                ));
            }
        }
    }
    /**
     * 随机抽取指定数量和面值限制的卡牌
     * @param nums 要抽取的卡牌数量，如果小于等于0则返回空列表
     * @param costs 卡牌面值上限，如果为-1则不限制面值
     * @return 随机抽取的卡牌列表，每张卡牌都是深拷贝的新对象，可重复抽取
     */
    public List<CardBattle>RandomGetCardsByNumAndCost(int nums,int costs) {
        //随机抽取nums张面值小于等于costs的卡牌
        //如果costs是-1那么就是全体随机
        // 1. 过滤符合条件的卡牌
        List<CardBattle> candidates = new ArrayList<>();
        for (CardBattle card : arrayList) {
            if (costs == -1 || card.getCardSize() <= costs) {
                candidates.add(card);
            }
        }
        // 2. 随机抽取nums张（可重复，不移除原卡牌）
        List<CardBattle> result = new ArrayList<>();
        if (candidates.isEmpty() || nums <= 0) {
            return result;
        }
        Random random = new Random();
        for (int i = 0; i < nums; i++) {
            CardBattle picked = candidates.get(random.nextInt(candidates.size()));
            // 深拷贝，避免影响原卡牌
            result.add(new CardBattle(
                    picked.getCardType(),
                    picked.getCardNum(),
                    picked.getCardName(),
                    picked.getCardSize()
            ));
        }
        return result;
    }

    /**
     * 随机删除列表中的指定数量卡牌，然后合并整理列表
     * @param list 要处理的卡牌列表，可以为null
     * @param x 要删除的卡牌数量，如果小于等于0则不删除任何卡牌
     * @return 处理后的卡牌列表，相同名称的卡牌会被合并，数量为0的卡牌会被移除
     */
    public List<CardBattle> RandomDiscardCardsList(List<CardBattle> list, int x) {
        // 处理边界情况
        if (list == null || list.isEmpty() || x <= 0) {
            return MergeCardList(list, null);
        }

        // 创建一个临时列表来存储要删除的卡牌（负数表示删除）
        List<CardBattle> discardList = new ArrayList<>();

        // 统计总卡牌数量
        int totalCards = 0;
        for (CardBattle card : list) {
            if (card != null) {
                totalCards += card.getCardNum();
            }
        }

        // 如果要删除的数量大于等于总数量，返回空列表
        if (x >= totalCards) {
            return new ArrayList<>();
        }

        // 创建一个展开的卡牌列表（每张卡牌按数量展开）
        List<CardBattle> expandedList = new ArrayList<>();
        for (CardBattle card : list) {
            if (card != null) {
                for (int i = 0; i < card.getCardNum(); i++) {
                    expandedList.add(new CardBattle(
                            card.getCardType(),
                            1, // 每个展开的卡牌数量为1
                            card.getCardName(),
                            card.getCardSize()
                    ));
                }
            }
        }

        // 随机删除x张卡牌
        Random random = new Random();
        for (int i = 0; i < x; i++) {
            if (!expandedList.isEmpty()) {
                int randomIndex = random.nextInt(expandedList.size());
                CardBattle removedCard = expandedList.remove(randomIndex);

                // 创建一个负数卡牌表示删除
                discardList.add(new CardBattle(
                        removedCard.getCardType(),
                        -1, // 负数表示删除
                        removedCard.getCardName(),
                        removedCard.getCardSize()
                ));
            }
        }

        // 使用MergeCardList合并原列表和删除列表
        return MergeCardList(list, discardList);
    }


    /**
     * 丢弃指定名称的卡牌
     * @param list 要处理的卡牌列表（注意：此方法会直接修改传入的列表）
     * @param a 要丢弃的卡牌名称
     * @return 处理后的卡牌列表，指定卡牌数量减少1张，数量为0的卡牌会被移除
     */
    public List<CardBattle> DiscardCard(List<CardBattle> list, String a) {
        CardBattle templateA = getByName(a);
        list.add(new CardBattle(
                templateA.getCardType(),
                -1,
                a,
                templateA.getCardSize()
        ));
        return MergeCardList(list, null);
    }

    /**
     * 获得指定名称的卡牌
     * @param list 要处理的卡牌列表（注意：此方法会直接修改传入的列表）
     * @param a 要获得的卡牌名称
     * @return 处理后的卡牌列表，指定卡牌数量增加1张
     */
    public List<CardBattle> GetCardByName(List<CardBattle> list, String a) {
        CardBattle templateA = getByName(a);
        list.add(templateA);
        return MergeCardList(list, null);
    }






}

package com.example.bg.GameBG.Play.Entities;

import lombok.Data;

@Data
public class CardBattle {
    /*包含的字段
    public String CardType;
    public int CardNum;有几张卡牌
    private int cardSize:卡牌面值

    */
    /*
    * cardType:
    *   battle，进攻
    *   defense，防守
    *   profit，增益
    *   decrease，减益
    * */

    public CardBattle(){}
    public CardBattle(String cardType,int cardNum,String cardName,int cardSize)
    {
        this.cardName=cardName;
        this.cardNum=cardNum;
        this.cardType=cardType;
        this.cardSize=cardSize;
    }
    private String cardType;
    private int cardNum;
    private String cardName;
    private int cardSize;

}


/*
*
* { key: 'love', src: new URL('../assets/cards/诗意/爱情.png', import.meta.url).href },
  { key: 'sad', src: new URL('../assets/cards/诗意/悲.png', import.meta.url).href },
  { key: 'spring', src: new URL('../assets/cards/诗意/春.png', import.meta.url).href },

  * { key: 'danbo', src: new URL('../assets/cards/诗意/淡泊.png', import.meta.url).href },
  { key: 'home', src: new URL('../assets/cards/诗意/故乡.png', import.meta.url).href },
  { key: 'yellowriver', src: new URL('../assets/cards/诗意/黄河.png', import.meta.url).href },

  * { key: 'fire', src: new URL('../assets/cards/诗意/火.png', import.meta.url).href },
  { key: 'wine', src: new URL('../assets/cards/诗意/酒.png', import.meta.url).href },
  { key: 'byebye', src: new URL('../assets/cards/诗意/离别.png', import.meta.url).href },
  { key: 'liu', src: new URL('../assets/cards/诗意/柳.png', import.meta.url).href },
  { key: 'bird', src: new URL('../assets/cards/诗意/鸟.png', import.meta.url).href },
  { key: 'autumn', src: new URL('../assets/cards/诗意/秋.png', import.meta.url).href },
  { key: 'sun', src: new URL('../assets/cards/诗意/日.png', import.meta.url).href },
  { key: 'mountain', src: new URL('../assets/cards/诗意/山.png', import.meta.url).href },
  { key: 'water', src: new URL('../assets/cards/诗意/水.png', import.meta.url).href },
  { key: 'missing', src: new URL('../assets/cards/诗意/思念.png', import.meta.url).href },
  { key: 'flower', src: new URL('../assets/cards/诗意/桃花.png', import.meta.url).href },
  { key: 'goose', src: new URL('../assets/cards/诗意/雁.png', import.meta.url).href },
  { key: 'friend', src: new URL('../assets/cards/诗意/友情.png', import.meta.url).href },
  { key: 'rain', src: new URL('../assets/cards/诗意/雨.png', import.meta.url).href },
  { key: 'moon', src: new URL('../assets/cards/诗意/月.png', import.meta.url).href },
  { key: 'war', src: new URL('../assets/cards/诗意/战争.png', import.meta.url).href },
  { key: 'longriver', src: new URL('../assets/cards/诗意/长江.png', import.meta.url).href },
  { key: 'bamboo', src: new URL('../assets/cards/诗意/竹.png', import.meta.url).href },
  { key: 'zhuangzhinanchou', src: new URL('../assets/cards/诗意/壮志难酬.png', import.meta.url).href },
  { key: 'nature', src: new URL('../assets/cards/诗意/自然.png', import.meta.url).href },
*
*
*
* */

package com.example.bg.GameBG.Player;


/*
* 增删查改
* */

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
@Mapper
@Repository
public interface PlayerOpMapper {
    void PlayerAdd(Player player);
    void PlayerDel(int UID,int PID);

    ArrayList<Player> PlayerFind(int UID);
    ArrayList<Integer>CardsFind(int PID);

    void updatePlayer(Player player);
    // 卡牌操作
    int checkCardExist(@Param("pid") int pid, @Param("cardType") String cardType);
    void updateCardNum(@Param("pid") int pid, @Param("cardType") String cardType,
                       @Param("cardNum") int cardNum);
    void insertCard(@Param("pid") int pid, @Param("cardType") String cardType,
                    @Param("cardNum") int cardNum);
    void deleteCard(@Param("pid") int pid, @Param("cardType") String cardType);

    // 批量卡牌操作
    void batchProcessCards(@Param("pid") int pid, @Param("cards") List<Card> cards);
}

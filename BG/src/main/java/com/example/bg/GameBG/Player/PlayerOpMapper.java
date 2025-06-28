package com.example.bg.GameBG.Player;


/*
* 增删查改
* */

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper
@Repository
public interface PlayerOpMapper {
    void PlayerAdd(Player player);
    void PlayerDel(int UID,int PID);

    ArrayList<Player> PlayerFind(int UID);
    ArrayList<Card>CardsFind(int PID);
    ArrayList<Player> PlayerFindByCondition(@Param("UID") int UID, @Param("PID") int PID);
    // 更新玩家基础信息
    void updatePlayerBase(PlayerDTO playerDTO);

    // 删除指定玩家的所有卡牌
    void deleteCardsByPID(int PID);

    // 批量插入卡牌数据
    void batchInsertCards(@Param("pid") int pid, @Param("cardList") List<PlayerDTO.CardDTO> cardList);
}

package com.example.bg.GameBG.Player;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PlayerService {
    @Autowired
    private  PlayerOpMapper playerOpMapper;
    //注册
    public void UserSignUp(int uid)
    {
        Player player=new Player();
        player.setAchievements("");
        player.setUID(uid);
        player.setGold(100);
        player.setCardList(new ArrayList<Card>());
        player.setLastPlayTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        playerOpMapper.PlayerAdd(player);
    }
    public void delUser(int UID,int PID)
    {
        playerOpMapper.PlayerDel(UID,PID);
    }
    public ArrayList<Player>playerFind(int UID)
    {
        ArrayList<Player>playerArrayList=playerOpMapper.PlayerFind(UID);

        return playerArrayList;
    }

    /*
    * 现在：存档读档
    * 读档：
    *   1.更新用户基础信息
    *   2.更新用户卡牌信息
    * 存档：
    *   1.存用户基础信息
    *   2.存用户卡牌信息
    * */
    /*
    * 步骤：
    * 1.收到当前信息，解码从controller传入service
    * 2.service实现对传入的player存档
    * 也就是存的player都是单独的player
    *
    * 复用：
    * 当传入的PID=-1，就是init
    * 否则根据PID and UID判断。
    * 都返回ArrayList
    *
    * */
    public ArrayList<Player>getPlayers(int UID,int PID)
    {
        ArrayList<Player>playerArrayList= PID == -1 ?
                playerOpMapper.PlayerFind(UID) :  // 当PID=-1时直接复用现有方法
                playerOpMapper.PlayerFindByCondition(UID, PID);
        for(Player player:playerArrayList)
        {
            player.cardList = playerOpMapper.CardsFind(player.PID);
            if(player.cardList == null)player.cardList=new ArrayList<Card>();
        }
        return playerArrayList;

    }
    @Transactional
    public void updateData(PlayerDTO playerDTO) {
        // 1. 参数校验
        if (playerDTO == null || playerDTO.getPID()  == null || playerDTO.getUID()  == null) {
            throw new IllegalArgumentException("无效的玩家数据");
        }

        // 2. 更新玩家基础信息
        playerOpMapper.updatePlayerBase(playerDTO);

        // 3. 更新卡牌信息（先删除后插入）
        if (playerDTO.getCardList()  != null && !playerDTO.getCardList().isEmpty())  {
            // 3.1 删除原有卡牌
            playerOpMapper.deleteCardsByPID(playerDTO.getPID());

            // 3.2 插入新卡牌
            playerOpMapper.batchInsertCards(playerDTO.getPID(),  playerDTO.getCardList());
        }

        // 4. 记录操作日志（可选）
        //log.info(" 玩家数据更新成功：UID={}, PID={}", playerDTO.getUID(),  playerDTO.getPID());
    }

}

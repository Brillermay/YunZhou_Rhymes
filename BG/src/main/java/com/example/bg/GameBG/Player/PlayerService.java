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
    public int UserSignUp(int uid)
    {
        Player player=new Player();
        player.setAchievements("");
        player.setUID(uid);
        player.setGold(100);
        player.setCardList(new ArrayList<Card>());
        player.setLastPlayTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
        playerOpMapper.PlayerAdd(player);
        return 0;
    }
    public void delUser(int UID,int PID)
    {
        playerOpMapper.PlayerDel(UID,PID);
    }
    public ArrayList<Player>playerFind(int UID)
    {
        return playerOpMapper.PlayerFind(UID);
    }
    // 保存玩家数据（含卡牌处理）
    @Transactional
    public void SaveData(Player player) {
        // 更新基础信息
        playerOpMapper.updatePlayer(player);

        // 批量处理卡牌
        if (player.getCardList()  != null && !player.getCardList().isEmpty())  {
            playerOpMapper.batchProcessCards(player.getPID(),  player.getCardList());
        }
    }

    // 单卡牌处理（可选）
    private void processCard(int pid, Card card) {
        if (playerOpMapper.checkCardExist(pid,  card.getCardType())  > 0) {
            if (card.getCardNum()  > 0) {
                playerOpMapper.updateCardNum(pid,  card.getCardType(),  card.getCardNum());
            } else {
                playerOpMapper.deleteCard(pid,  card.getCardType());
            }
        } else if (card.getCardNum()  > 0) {
            playerOpMapper.insertCard(pid,  card.getCardType(),  card.getCardNum());
        }
    }
}

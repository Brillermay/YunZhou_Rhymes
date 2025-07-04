package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class InitService {
    /*
    * 处理初始化相关的函数
    * 创建房间
    * 加入房间
    * 初始化用户
    * */
    public Room CreateRoom(int uid1)
    {
        Room now=new Room();
        now.setRoom(RandomStringUtils.randomAlphanumeric(16));
        now.setUid1(uid1);
        now.setUid2(0);
        now.setRoundNum(0);
        return now;
    }
    public Room joinRoom(Room now,int uid2)
    {
        now.setUid2(uid2);
        return now;
    }
    public PlayerAgainst init(String roomId, int userId, String role,CardService cardService)
    {
        PlayerAgainst playerAgainst =new PlayerAgainst();
        playerAgainst.setHp(20);
        playerAgainst.setHpMax(20);
        playerAgainst.setShieldMax(20);
        playerAgainst.setRoomNumber(roomId);
        List<CardBattle> cardList=new ArrayList<>();
        playerAgainst.setCards(cardList);
        playerAgainst.setWealthy(10);
        List<Status>statuses=new ArrayList<>();
        playerAgainst.setStatusesEnd(statuses);
        playerAgainst.setRole(role);
        playerAgainst.setShield(20);
        return playerAgainst;
    }
}

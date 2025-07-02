package com.example.bg.GameBG.Play;

import com.example.bg.GameBG.Play.Entities.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class PlayService {
    /*
    * 创建/销毁房间
    * 获取基本信息
    * 每回合结算
    * 操作
    * 广播数据
    * */
    @Autowired
    BattleMapper battleMapper;
    Map<String, Room> roomMap = new ConcurrentHashMap<>();
    Map<Integer, PlayerAgainst>playerAgainstMap = new ConcurrentHashMap<>();
    public Room CreateRoom(int uid1)
    {
        Room now=new Room();
        now.setRoom(RandomStringUtils.randomAlphanumeric(16));
        now.setUid1(uid1);
        now.setUid2(0);
        roomMap.put(now.getRoom(),now);
        return now;
    }
    public String killRoom(String roomId)
    {
        Room now = roomMap.get(roomId);
        //保存到数据库
        HistoryDTO historyDTO=new HistoryDTO();
        battleMapper.saveCache(historyDTO);
        return "退出成功";
    }
    public Room joinRoom(String roomId,int uid2)
    {
        Room now=roomMap.get(roomId);
        now.setUid2(uid2);
        return now;
    }
    public PlayerAgainst init(String roomId,int userId,String role)
    {
        List<CardBattle>cardList=new ArrayList<>();
        List<Status>statuses=new ArrayList<>();
        //初始化cardList

        PlayerAgainst playerAgainst =new PlayerAgainst();
        playerAgainst.setHp(100);
        playerAgainst.setCards(cardList);
        playerAgainst.setRoomNumber(roomId);
        playerAgainst.setWealthy(100);
        playerAgainst.setStatuses(statuses);

        return playerAgainst;
    }

}

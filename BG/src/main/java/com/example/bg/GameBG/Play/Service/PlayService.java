package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.BattleMapper;
import com.example.bg.GameBG.Play.Entities.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
@Deprecated
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

    public String killRoom(String roomId)
    {
        Room now = roomMap.get(roomId);
        //保存到数据库
        HistoryDTO historyDTO=new HistoryDTO();
        battleMapper.saveCache(historyDTO);
        return "退出成功";
    }


}

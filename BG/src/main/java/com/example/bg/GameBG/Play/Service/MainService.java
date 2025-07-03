package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
import com.example.bg.GameBG.Play.Entities.Room;
import com.example.bg.GameBG.Play.Entities.RoundEndDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MainService {
    //房间号-room的映射
    Map<String, Room> roomMap = new ConcurrentHashMap<>();
    //用户id-playerAgainst的映射
    Map<Integer, PlayerAgainst>playerAgainstMap = new ConcurrentHashMap<>();
    //回合数-每回合结束发的数据映射
    Map<Integer, RoundEndDTO>roundEndDTOMapHistory=new ConcurrentHashMap<>();


}

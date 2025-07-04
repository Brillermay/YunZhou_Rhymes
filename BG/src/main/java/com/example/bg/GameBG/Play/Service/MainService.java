package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
import com.example.bg.GameBG.Play.Entities.Room;
import com.example.bg.GameBG.Play.Entities.RoundEndDTO;
import kotlin.Pair;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MainService {
    //房间号-room的映射
    Map<String, Room> roomMap = new ConcurrentHashMap<>();
    //用户id-playerAgainst的映射
    Map<Integer, PlayerAgainst>playerAgainstMap = new ConcurrentHashMap<>();
    //回合数-根据pair<房间，回合数>进行查找
    Map<Pair<String,Integer>, RoundEndDTO>roundEndDTOMapHistory=new ConcurrentHashMap<>();
    /*
    *
    * */
    PlayerService playerService=new PlayerService();
    InitService initService=new InitService();
    public String CreateRoom(int userId)
    {
        Room room=initService.CreateRoom(userId);
        String roomId=room.getRoom();
        roomMap.put(roomId,room);
        return roomId;
    }
    public String JoinRoom(String roomId,int userId){
        if(roomMap.containsKey(roomId))
        {
            initService.joinRoom(roomMap.get(roomId),userId);
            return "加入成功!";
        }
        else return "没找到房间！";
    }
    public String StartGame(String roomId,String role1,String role2){
        if(roomMap.containsKey(roomId))
        {
            if(roomMap.get(roomId).getUid2() == -1)return "房间人数没满！";
            Room room=roomMap.get(roomId);
            PlayerAgainst playerAgainst1=initService.init(roomId,room.getUid1(),role1);
            PlayerAgainst playerAgainst2=initService.init(roomId,room.getUid2(),role2);
            playerAgainstMap.put(room.getUid1(),playerAgainst1);
            playerAgainstMap.put(room.getUid2(),playerAgainst2);
            return "开始成功";
        }
        else return "没找到房间！";
    }
}

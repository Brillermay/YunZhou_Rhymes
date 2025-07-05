package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
import com.example.bg.GameBG.Play.Entities.Room;
import com.example.bg.GameBG.Play.Entities.RoundEndDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import kotlin.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MainService extends TextWebSocketHandler {

    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

    }
    private void broadcast(String msg) {
//        for (WebSocketSession s : sessions) {
//            try {
//                s.sendMessage(new TextMessage(msg));
//            } catch (Exception ignored) {}
//        }
    }
    private String gameStateToJson() {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> map = new HashMap<>();
//            map.put("gameState", gameState);
//            return mapper.writeValueAsString(map);
//        } catch (Exception e) {
//            return "{\"gameState\":[]}";
//        }
        return "";
    }

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
    public void SynthesizeCards(int uid,String a,String b,String c){
        PlayerAgainst playerAgainst = playerAgainstMap.get(uid);
        playerService.SynthesizeABC(playerAgainst,a,b,c);
    }


}

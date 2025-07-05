package com.example.bg.GameBG.Play.Service;

import com.example.bg.GameBG.Play.Entities.CardBattle;
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
    /**
     * 创建房间
     * @param userId 用户ID
     * @return 返回房间ID
     */
    public String CreateRoom(int userId)
    {
        Room room=initService.CreateRoom(userId);
        String roomId=room.getRoom();
        roomMap.put(roomId,room);
        return roomId;
    }

    /**
     * 加入房间
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 返回加入结果信息
     */
    public String JoinRoom(String roomId,int userId){
        if(roomMap.containsKey(roomId))
        {
            initService.joinRoom(roomMap.get(roomId),userId);
            return "加入成功!";
        }
        else return "没找到房间！";
    }

    /**
     * 开始游戏
     * @param roomId 房间ID
     * @param role1 玩家1角色
     * @param role2 玩家2角色
     * @return 返回游戏开始结果信息
     */
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

    /**
     * 合成卡牌
     * @param uid 用户ID
     * @param a 卡牌A
     * @param b 卡牌B
     * @param c 卡牌C
     */
    public void SynthesizeCards(int uid,String a,String b,String c){
        PlayerAgainst playerAgainst = playerAgainstMap.get(uid);
        playerService.SynthesizeABC(playerAgainst,a,b,c);
    }

    /**
     * 执行一轮游戏
     * @param listPlayer1 玩家1卡牌列表
     * @param listPlayer2 玩家2卡牌列表
     * @param roomId 房间ID
     */
    public void RunARound(List<CardBattle>listPlayer1, List<CardBattle>listPlayer2,
                          String roomId){
        Room room=roomMap.get(roomId);
        PlayerAgainst playerAgainst1=playerAgainstMap.get(room.getUid1());
        PlayerAgainst playerAgainst2=playerAgainstMap.get(room.getUid2());
        playerService.MainService(playerAgainst1,
                playerAgainst2,
                listPlayer1,
                listPlayer2,
                room);
    }

}

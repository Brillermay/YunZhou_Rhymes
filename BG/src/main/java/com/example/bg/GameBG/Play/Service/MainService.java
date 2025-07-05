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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket连接已建立，当前连接数：" + sessions.size());

        Map<String, Object> response = new HashMap<>();
        response.put("type", "connection");
        response.put("message", "连接成功");
        response.put("sessionId", session.getId());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            System.out.println("收到消息：" + payload);

            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String type = (String) messageData.get("type");

            switch (type) {
                case "synthesize":
                    handleSynthesizeMessage(session, messageData);
                    break;
                case "createRoom":
                    handleCreateRoomMessage(session, messageData);
                    break;
                case "joinRoom":
                    handleJoinRoomMessage(session, messageData);
                    break;
                case "startGame":
                    handleStartGameMessage(session, messageData);
                    break;
                default:
                    sendErrorResponse(session, "未知的消息类型：" + type);
            }

        } catch (Exception e) {
            System.err.println("处理消息时发生错误：" + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(session, "消息处理失败：" + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("WebSocket连接已关闭，当前连接数：" + sessions.size());
    }
    /**
     * 处理合成卡牌消息
     * @param session WebSocket会话
     * @param messageData 消息数据
            {
            "type":"synthesize",
            "room":{
            "uid":"5",
            "cardA":"Aname",
            "cardB":"Bname",
            "cardC":"Cname"
            }
      }，
     * @throws Exception 异常
     *
     */
    private void handleSynthesizeMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String uidStr = (String) roomData.get("uid");
        int uid = Integer.parseInt(uidStr);
        String cardA = (String) roomData.get("cardA");
        String cardB = (String) roomData.get("cardB");
        String cardC = (String) roomData.get("cardC");

        SynthesizeCards(uid, cardA, cardB, cardC);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "synthesize_result");
        response.put("success", true);
        response.put("message", "卡牌合成成功");
        response.put("uid", uid);
        response.put("cards", Map.of("cardA", cardA, "cardB", cardB, "cardC", cardC));

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleCreateRoomMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String uidStr = (String) roomData.get("uid");
        int uid = Integer.parseInt(uidStr);

        String roomId = CreateRoom(uid);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "create_room_result");
        response.put("success", true);
        response.put("roomId", roomId);
        response.put("uid", uid);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleJoinRoomMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String roomId = (String) roomData.get("roomId");
        String uidStr = (String) roomData.get("uid");
        int uid = Integer.parseInt(uidStr);

        String result = JoinRoom(roomId, uid);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "join_room_result");
        response.put("success", result.contains("成功"));
        response.put("message", result);
        response.put("roomId", roomId);
        response.put("uid", uid);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleStartGameMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String roomId = (String) roomData.get("roomId");
        String role1 = (String) roomData.get("role1");
        String role2 = (String) roomData.get("role2");

        String result = StartGame(roomId, role1, role2);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "start_game_result");
        response.put("success", result.contains("成功"));
        response.put("message", result);
        response.put("roomId", roomId);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "error");
        response.put("success", false);
        response.put("message", errorMessage);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void broadcast(String msg) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(msg));
                }
            } catch (Exception e) {
                System.err.println("广播消息失败：" + e.getMessage());
            }
        }
    }

    private String gameStateToJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            map.put("type", "game_state");
            map.put("rooms", roomMap.size());
            map.put("players", playerAgainstMap.size());
            map.put("timestamp", System.currentTimeMillis());
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"type\":\"game_state\",\"error\":\"状态获取失败\"}";
        }
    }

    //房间号-room的映射
    Map<String, Room> roomMap = new ConcurrentHashMap<>();
    //用户id-playerAgainst的映射
    Map<Integer, PlayerAgainst>playerAgainstMap = new ConcurrentHashMap<>();
    //回合数-根据pair<房间，回合数>进行查找
    Map<Pair<String,Integer>, RoundEndDTO>roundEndDTOMapHistory=new ConcurrentHashMap<>();

    PlayerService playerService=new PlayerService();
    InitService initService=new InitService();

    /**
     * 创建房间
     * @param userId 用户ID
     * @return 返回房间ID
     */
    public String CreateRoom(int userId) {
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
        if(roomMap.containsKey(roomId)) {
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
        if(roomMap.containsKey(roomId)) {
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
        if(playerAgainst != null) {
            playerService.SynthesizeABC(playerAgainst,a,b,c);
        }
    }

    /**
     * 执行一轮游戏
     * @param listPlayer1 玩家1卡牌列表
     * @param listPlayer2 玩家2卡牌列表
     * @param roomId 房间ID
     */
    public void RunARound(List<CardBattle>listPlayer1, List<CardBattle>listPlayer2, String roomId){
        Room room=roomMap.get(roomId);
        PlayerAgainst playerAgainst1=playerAgainstMap.get(room.getUid1());
        PlayerAgainst playerAgainst2=playerAgainstMap.get(room.getUid2());
        playerService.MainService(playerAgainst1, playerAgainst2, listPlayer1, listPlayer2, room);
    }
}


//package com.example.bg.GameBG.Play.Service;
//
//import com.example.bg.GameBG.Play.Entities.CardBattle;
//import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
//import com.example.bg.GameBG.Play.Entities.Room;
//import com.example.bg.GameBG.Play.Entities.RoundEndDTO;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import kotlin.Pair;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.stereotype.Service;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//@Service
//public class MainService extends TextWebSocketHandler {
//
//    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    /**
//     * WebSocket连接建立时调用
//     * @param session WebSocket会话
//     * @throws Exception 异常
//     */
//    @Override
//    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
//        sessions.add(session);
//        System.out.println("WebSocket连接已建立，当前连接数：" + sessions.size());
//
//        // 向客户端发送连接成功消息
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "connection");
//        response.put("message", "连接成功");
//        response.put("sessionId", session.getId());
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//    }
//
//    /**
//     * 处理接收到的WebSocket消息
//     * @param session WebSocket会话
//     * @param message 接收到的消息
//     * @throws Exception 异常
//     */
//    @Override
//    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws Exception {
//        try {
//            // 1. 解析前端发送的JSON消息
//            String payload = message.getPayload();
//            System.out.println("收到消息：" + payload);
//
//            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
//            String type = (String) messageData.get("type");
//
//            // 2. 根据消息类型分发处理
//            switch (type) {
//                case "synthesize"://合成
//                    handleSynthesizeMessage(session, messageData);
//                    break;
//                case "startGame"://启动游戏
//                    handleStartGameMessage(session, messageData);
//                    break;
//                default:
//                    sendErrorResponse(session, "未知的消息类型：" + type);
//            }
//
//        } catch (Exception e) {
//            System.err.println("处理消息时发生错误：" + e.getMessage());
//            sendErrorResponse(session, "消息处理失败：" + e.getMessage());
//        }
//    }
//

//    private void handleSynthesizeMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        /*
//        * {
//            "type":"synthesize",
//            "room":{
//                "uid":"5",
//                "cardA":"Aname",
//                "cardB":"Bname",
//                "cardC":"Cname"
//            }
//           }，
//        *
//        *
//        * */
//
//        // 解析消息中的room数据
//        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
//
//        // 提取参数
//        String uidStr = (String) roomData.get("uid");
//        int uid = Integer.parseInt(uidStr);
//        String cardA = (String) roomData.get("cardA");
//        String cardB = (String) roomData.get("cardB");
//        String cardC = (String) roomData.get("cardC");
//
//        // 调用业务逻辑
//        SynthesizeCards(uid, cardA, cardB, cardC);
//
//        // 返回成功响应
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "synthesize_result");
//        response.put("success", true);
//        response.put("message", "卡牌合成成功");
//        response.put("uid", uid);
//        response.put("cards", Map.of("cardA", cardA, "cardB", cardB, "cardC", cardC));
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//
//        System.out.println("用户" + uid + "合成卡牌：" + cardA + "+" + cardB + "+" + cardC);
//    }
//
//    /**
//     * 处理开始游戏消息
//     * @param session WebSocket会话
//     * @param messageData 消息数据
//     *
//     *                    {
//     *                 "type":"startGame",
//     *                  "room":{
//     *                      "roomId":"aaaaaaaaaaaaaaaa",
//     *                      "role1":"libai",
//     *                       "role2":"wangwei"
//     *                   }
//     *        }，
//     * @throws Exception 异常
//     */
//    private void handleStartGameMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
//        String roomId = (String) roomData.get("roomId");
//        String role1 = (String) roomData.get("role1");
//        String role2 = (String) roomData.get("role2");
//
//        String result = StartGame(roomId, role1, role2);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "start_game_result");
//        response.put("success", result.contains("成功"));
//        response.put("message", result);
//        response.put("roomId", roomId);
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//    }
//
//    /**
//     * 发送错误响应
//     * @param session WebSocket会话
//     * @param errorMessage 错误消息
//     * @throws Exception 异常
//     */
//    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "error");
//        response.put("success", false);
//        response.put("message", errorMessage);
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//    }
//
//    /**
//     * WebSocket连接关闭时调用
//     * @param session WebSocket会话
//     * @param status 关闭状态
//     */
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        sessions.remove(session);
//        System.out.println("WebSocket连接已关闭，当前连接数：" + sessions.size());
//    }
//
//    /**
//     * 向所有连接的客户端广播消息
//     * @param msg 要广播的消息
//     */
//    private void broadcast(String msg) {
//        for (WebSocketSession session : sessions) {
//            try {
//                if (session.isOpen()) {
//                    session.sendMessage(new TextMessage(msg));
//                }
//            } catch (Exception e) {
//                System.err.println("广播消息失败：" + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * 将游戏状态转换为JSON字符串
//     * @return JSON字符串
//     */
//    private String gameStateToJson() {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> map = new HashMap<>();
//            map.put("type", "game_state");
//            map.put("rooms", roomMap.size());
//            map.put("players", playerAgainstMap.size());
//            map.put("timestamp", System.currentTimeMillis());
//            return mapper.writeValueAsString(map);
//        } catch (Exception e) {
//            return "{\"type\":\"game_state\",\"error\":\"状态获取失败\"}";
//        }
//    }
//
//    // ...existing code...
//    //房间号-room的映射
//    Map<String, Room> roomMap = new ConcurrentHashMap<>();
//    //用户id-playerAgainst的映射
//    Map<Integer, PlayerAgainst>playerAgainstMap = new ConcurrentHashMap<>();
//    //回合数-根据pair<房间，回合数>进行查找
//    Map<Pair<String,Integer>, RoundEndDTO>roundEndDTOMapHistory=new ConcurrentHashMap<>();
//    /*
//     *
//     * */
//    PlayerService playerService=new PlayerService();
//    InitService initService=new InitService();
//    /**
//     * 创建房间
//     * @param userId 用户ID
//     * @return 返回房间ID
//     */
//    public String CreateRoom(int userId)
//    {
//        Room room=initService.CreateRoom(userId);
//        String roomId=room.getRoom();
//        roomMap.put(roomId,room);
//        return roomId;
//    }
//
//    /**
//     * 加入房间
//     * @param roomId 房间ID
//     * @param userId 用户ID
//     * @return 返回加入结果信息
//     */
//    public String JoinRoom(String roomId,int userId){
//        if(roomMap.containsKey(roomId))
//        {
//            initService.joinRoom(roomMap.get(roomId),userId);
//            return "加入成功!";
//        }
//        else return "没找到房间！";
//    }
//
//    /**
//     * 开始游戏
//     * @param roomId 房间ID
//     * @param role1 玩家1角色
//     * @param role2 玩家2角色
//     * @return 返回游戏开始结果信息
//     */
//    public String StartGame(String roomId,String role1,String role2){
//        if(roomMap.containsKey(roomId))
//        {
//            if(roomMap.get(roomId).getUid2() == -1)return "房间人数没满！";
//            Room room=roomMap.get(roomId);
//            PlayerAgainst playerAgainst1=initService.init(roomId,room.getUid1(),role1);
//            PlayerAgainst playerAgainst2=initService.init(roomId,room.getUid2(),role2);
//            playerAgainstMap.put(room.getUid1(),playerAgainst1);
//            playerAgainstMap.put(room.getUid2(),playerAgainst2);
//            return "开始成功";
//        }
//        else return "没找到房间！";
//    }
//
//    /**
//     * 合成卡牌
//     * @param uid 用户ID
//     * @param a 卡牌A
//     * @param b 卡牌B
//     * @param c 卡牌C
//     */
//    public void SynthesizeCards(int uid,String a,String b,String c){
//        PlayerAgainst playerAgainst = playerAgainstMap.get(uid);
//        playerService.SynthesizeABC(playerAgainst,a,b,c);
//    }
//
//    /**
//     * 执行一轮游戏
//     * @param listPlayer1 玩家1卡牌列表
//     * @param listPlayer2 玩家2卡牌列表
//     * @param roomId 房间ID
//     */
//    public void RunARound(List<CardBattle>listPlayer1, List<CardBattle>listPlayer2,
//                          String roomId){
//        Room room=roomMap.get(roomId);
//        PlayerAgainst playerAgainst1=playerAgainstMap.get(room.getUid1());
//        PlayerAgainst playerAgainst2=playerAgainstMap.get(room.getUid2());
//        playerService.MainService(playerAgainst1,
//                playerAgainst2,
//                listPlayer1,
//                listPlayer2,
//                room);
//    }
//
//}

//package com.example.bg.GameBG.Play.Service;
//
//import com.example.bg.GameBG.Play.Entities.CardBattle;
//import com.example.bg.GameBG.Play.Entities.PlayerAgainst;
//import com.example.bg.GameBG.Play.Entities.Room;
//import com.example.bg.GameBG.Play.Entities.RoundEndDTO;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import kotlin.Pair;
//import org.springframework.stereotype.Service;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//@Service
//public class MainService extends TextWebSocketHandler {
//
//    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//
//    }
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//
//    }
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//
//    }
//    private void broadcast(String msg) {
////        for (WebSocketSession s : sessions) {
////            try {
////                s.sendMessage(new TextMessage(msg));
////            } catch (Exception ignored) {}
////        }
//    }
//    private String gameStateToJson() {
////        try {
////            ObjectMapper mapper = new ObjectMapper();
////            Map<String, Object> map = new HashMap<>();
////            map.put("gameState", gameState);
////            return mapper.writeValueAsString(map);
////        } catch (Exception e) {
////            return "{\"gameState\":[]}";
////        }
//        return "";
//    }
//
//    //房间号-room的映射
//    Map<String, Room> roomMap = new ConcurrentHashMap<>();
//    //用户id-playerAgainst的映射
//    Map<Integer, PlayerAgainst>playerAgainstMap = new ConcurrentHashMap<>();
//    //回合数-根据pair<房间，回合数>进行查找
//    Map<Pair<String,Integer>, RoundEndDTO>roundEndDTOMapHistory=new ConcurrentHashMap<>();
//    /*
//    *
//    * */
//    PlayerService playerService=new PlayerService();
//    InitService initService=new InitService();
//    /**
//     * 创建房间
//     * @param userId 用户ID
//     * @return 返回房间ID
//     */
//    public String CreateRoom(int userId)
//    {
//        Room room=initService.CreateRoom(userId);
//        String roomId=room.getRoom();
//        roomMap.put(roomId,room);
//        return roomId;
//    }
//
//    /**
//     * 加入房间
//     * @param roomId 房间ID
//     * @param userId 用户ID
//     * @return 返回加入结果信息
//     */
//    public String JoinRoom(String roomId,int userId){
//        if(roomMap.containsKey(roomId))
//        {
//            initService.joinRoom(roomMap.get(roomId),userId);
//            return "加入成功!";
//        }
//        else return "没找到房间！";
//    }
//
//    /**
//     * 开始游戏
//     * @param roomId 房间ID
//     * @param role1 玩家1角色
//     * @param role2 玩家2角色
//     * @return 返回游戏开始结果信息
//     */
//    public String StartGame(String roomId,String role1,String role2){
//        if(roomMap.containsKey(roomId))
//        {
//            if(roomMap.get(roomId).getUid2() == -1)return "房间人数没满！";
//            Room room=roomMap.get(roomId);
//            PlayerAgainst playerAgainst1=initService.init(roomId,room.getUid1(),role1);
//            PlayerAgainst playerAgainst2=initService.init(roomId,room.getUid2(),role2);
//            playerAgainstMap.put(room.getUid1(),playerAgainst1);
//            playerAgainstMap.put(room.getUid2(),playerAgainst2);
//            return "开始成功";
//        }
//        else return "没找到房间！";
//    }
//
//    /**
//     * 合成卡牌
//     * @param uid 用户ID
//     * @param a 卡牌A
//     * @param b 卡牌B
//     * @param c 卡牌C
//     */
//    public void SynthesizeCards(int uid,String a,String b,String c){
//        PlayerAgainst playerAgainst = playerAgainstMap.get(uid);
//        playerService.SynthesizeABC(playerAgainst,a,b,c);
//    }
//
//    /**
//     * 执行一轮游戏
//     * @param listPlayer1 玩家1卡牌列表
//     * @param listPlayer2 玩家2卡牌列表
//     * @param roomId 房间ID
//     */
//    public void RunARound(List<CardBattle>listPlayer1, List<CardBattle>listPlayer2,
//                          String roomId){
//        Room room=roomMap.get(roomId);
//        PlayerAgainst playerAgainst1=playerAgainstMap.get(room.getUid1());
//        PlayerAgainst playerAgainst2=playerAgainstMap.get(room.getUid2());
//        playerService.MainService(playerAgainst1,
//                playerAgainst2,
//                listPlayer1,
//                listPlayer2,
//                room);
//    }
//
//}

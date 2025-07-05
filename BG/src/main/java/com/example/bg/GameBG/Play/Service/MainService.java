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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MainService extends TextWebSocketHandler {

    Map<String,Integer>roomNum=new ConcurrentHashMap<>();
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    // 添加用户ID到会话的映射
    private static final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();
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

            // 从消息中尝试获取用户ID并关联到会话
            if (messageData.containsKey("room") && ((Map)messageData.get("room")).containsKey("uid")) {
                String uidStr = (String) ((Map)messageData.get("room")).get("uid");
                try {
                    int uid = Integer.parseInt(uidStr);
                    userSessions.put(uid, session);
                    System.out.println("关联用户ID " + uid + " 到会话 " + session.getId());
                } catch (NumberFormatException e) {
                    System.err.println("无法解析用户ID: " + e.getMessage());
                }
            }

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
                case "RoundBegin":
                    handleRoundBeginMessage(session,messageData);
                    break;
                case "RoundEnd":
                    handleRoundEndMessage(session,messageData);
                    break;
                case "getRoomStatus":
                    handleGetRoomStatusMessage(session, messageData);
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

        // 清理用户会话映射
        for (Map.Entry<Integer, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                userSessions.remove(entry.getKey());
                System.out.println("用户 " + entry.getKey() + " 的会话已关闭");
                break;
            }
        }

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

    /**
     * {"type":"createRoom","room":{"uid":"1"}}
     *
     * */
    private void handleCreateRoomMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String uidStr = (String) roomData.get("uid");
        int uid = Integer.parseInt(uidStr);

        // 关联用户ID到会话
        userSessions.put(uid, session);

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

        // 关联用户ID到会话
        userSessions.put(uid, session);

        String result = JoinRoom(roomId, uid);
        boolean success = result.contains("成功");

        // 解析玩家数量
        int playerCount = 0;
        if (success && result.contains("#")) {
            String[] parts = result.split("#");
            if (parts.length > 1) {
                try {
                    playerCount = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("解析玩家数量失败: " + e.getMessage());
                }
            }
        }

        // 获取房间信息
        Room room = null;
        if (roomMap.containsKey(roomId)) {
            room = roomMap.get(roomId);

            // 检查：如果是同一用户加入自己创建的房间，直接告知用户
            if (room.getUid1() == uid && room.getUid2() == uid) {
                Map<String, Object> warningMsg = new HashMap<>();
                warningMsg.put("type", "room_warning");
                warningMsg.put("message", "您已经在该房间中，无需重复加入");
                warningMsg.put("roomId", roomId);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(warningMsg)));
            }
        }

        // 计算真实的不重复玩家数量
        Set<Integer> uniqueUsers = new HashSet<>();
        if (room != null) {
            if (room.getUid1() > 0) uniqueUsers.add(room.getUid1());
            if (room.getUid2() > 0) uniqueUsers.add(room.getUid2());
        }
        int uniquePlayerCount = uniqueUsers.size();

        Map<String, Object> response = new HashMap<>();
        response.put("type", "join_room_result");
        response.put("success", success);
        response.put("message", result.split("#")[0]);
        response.put("roomId", roomId);
        response.put("uid", uid);
        response.put("playerCount", uniquePlayerCount);  // 使用不重复计算的玩家数
        response.put("isFull", uniquePlayerCount >= 2);
        response.put("readyToStart", uniquePlayerCount >= 2);

        // 添加房间内的用户信息
        if (room != null) {
            response.put("roomOwner", room.getUid1());
            response.put("roomJoiner", room.getUid2());
            response.put("uniqueUsers", new ArrayList<>(uniqueUsers));
        }

        // 发送响应
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        // 如果房间已满，再次直接发送一条更新消息给当前用户
        if (playerCount >= 2) {
            Map<String, Object> fullRoomMsg = new HashMap<>();
            fullRoomMsg.put("type", "room_update");
            fullRoomMsg.put("roomId", roomId);
            fullRoomMsg.put("playerCount", playerCount);
            fullRoomMsg.put("isFull", true);
            fullRoomMsg.put("readyToStart", true);
            fullRoomMsg.put("message", "房间已满，准备开始游戏");

            // 确保当前用户收到这条消息
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(fullRoomMsg)));
            System.out.println("发送房间已满通知给用户: " + uid);
        }
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

    private void handleRoundBeginMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        //调用playerService.beginService,false
        //发送roomId,发送uid，均为true再执行
    }

    private void handleRoundEndMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        //调用playerService.MainService
        //需要：当前卡牌的英文名，当前roomId
        //同步所有的
    }

    private void handleGetRoomStatusMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String roomId = (String) roomData.get("roomId");

        if (roomMap.containsKey(roomId)) {
            Room room = roomMap.get(roomId);
            int playerCount = 0;
            if (room.getUid1() > 0) playerCount++;
            if (room.getUid2() > 0) playerCount++;

            Map<String, Object> response = new HashMap<>();
            response.put("type", "room_status_result");
            response.put("roomId", roomId);
            response.put("playerCount", playerCount);
            response.put("isFull", playerCount >= 2);
            response.put("users", Arrays.asList(room.getUid1(), room.getUid2()));

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

            // 如果房间已满，同时广播一条通知消息
            if (playerCount >= 2) {
                Map<String, Object> updateMsg = new HashMap<>();
                updateMsg.put("type", "room_update");
                updateMsg.put("roomId", roomId);
                updateMsg.put("playerCount", playerCount);
                updateMsg.put("isFull", true);
                updateMsg.put("readyToStart", true);

                broadcast(objectMapper.writeValueAsString(updateMsg));
            }
        } else {
            sendErrorResponse(session, "房间不存在：" + roomId);
        }
    }

    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "error");
        response.put("success", false);
        response.put("message", errorMessage);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void broadcast(String msg) {
        System.out.println("广播消息: " + msg); // 添加日志便于调试

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(msg));
                }
            } catch (Exception e) {
                System.err.println("广播消息失败到会话 " + session.getId() + ": " + e.getMessage());
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
    /**
     * 加入房间
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 返回加入结果信息
     */
    public String JoinRoom(String roomId, int userId) {
        if (roomMap.containsKey(roomId)) {
            Room room = roomMap.get(roomId);
            initService.joinRoom(room, userId);

            // 修正：计算真实的不重复玩家数量
            int playerCount = 0;
            // 使用Set来确保不重复计算
            Set<Integer> uniqueUsers = new HashSet<>();
            if (room.getUid1() > 0) uniqueUsers.add(room.getUid1());
            if (room.getUid2() > 0) uniqueUsers.add(room.getUid2());
            playerCount = uniqueUsers.size();

            roomNum.put(roomId, playerCount);

            try {
                // 创建房间更新消息
                Map<String, Object> roomUpdateMsg = new HashMap<>();
                roomUpdateMsg.put("type", "room_update");
                roomUpdateMsg.put("roomId", roomId);
                roomUpdateMsg.put("playerCount", playerCount);
                roomUpdateMsg.put("isFull", playerCount >= 2);
                roomUpdateMsg.put("joinedUserId", userId);
                roomUpdateMsg.put("readyToStart", playerCount >= 2);
                roomUpdateMsg.put("uniqueUsers", new ArrayList<>(uniqueUsers));  // 添加唯一用户列表以便调试

                String updateMsg = objectMapper.writeValueAsString(roomUpdateMsg);

                // 发送给房间内的所有用户
                for (int uid : uniqueUsers) {
                    if (userSessions.containsKey(uid)) {
                        WebSocketSession userSession = userSessions.get(uid);
                        if (userSession.isOpen()) {
                            userSession.sendMessage(new TextMessage(updateMsg));
                            System.out.println("直接发送房间更新给用户(uid=" + uid + ")");
                        }
                    }
                }

                // 备份通知机制
                broadcast(updateMsg);

            } catch (Exception e) {
                System.err.println("发送房间更新失败: " + e.getMessage());
            }

            return "加入成功!#" + playerCount;
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
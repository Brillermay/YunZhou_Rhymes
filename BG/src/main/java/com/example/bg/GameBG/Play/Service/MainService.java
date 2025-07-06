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

/**
 * 主服务类，处理WebSocket通信和游戏逻辑
 * 负责房间管理、用户连接管理和游戏状态控制
 */
@Service
public class MainService extends TextWebSocketHandler {

    Map<String,Integer>roomNum=new ConcurrentHashMap<>();
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    // 添加用户ID到会话的映射
    private static final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Pair<String, Integer>, Set<Integer>> roundBeginSyncMap = new ConcurrentHashMap<>();

    /**
     * WebSocket连接建立后的处理方法
     * 当客户端成功连接到服务器时调用
     *
     * @param session 建立的WebSocket会话
     * @throws Exception 连接过程中可能发生的异常
     */
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

    /**
     * 处理从客户端接收到的文本消息
     * 根据消息类型分发到不同的处理函数
     *
     * @param session 接收消息的WebSocket会话
     * @param message 接收到的文本消息
     * @throws Exception 消息处理过程中可能发生的异常
     */
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
                case "openCardGroups":
                    handleOpenCardGroups(session,messageData);
                    break;
                case "discardCard":
                    handleDiscardCard(session,messageData);
                    break;
                case "fetchall":
                    handleFetchAll(session,messageData);
                default:
                    sendErrorResponse(session, "未知的消息类型：" + type);
            }

        } catch (Exception e) {
            System.err.println("处理消息时发生错误：" + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(session, "消息处理失败：" + e.getMessage());
        }
    }

    /**
     * WebSocket连接关闭后的处理方法
     * 清理会话相关资源并更新连接状态
     *
     * @param session 关闭的WebSocket会话
     * @param status 连接关闭状态
     */
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
     * 处理 fetchall 消息
     * 功能：解析发过来的 roomId，打印并广播所有的消息：比如当前房间的有谁，每个人的状态又是什么样的
     * 前端发送格式: {"type":"fetchall","room":{"roomId":"xxx"}}
     */
    private void handleFetchAll(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String roomId = (String) roomData.get("roomId");

        if (!roomMap.containsKey(roomId)) {
            sendErrorResponse(session, "房间不存在：" + roomId);
            return;
        }

        Room room = roomMap.get(roomId);

        // 获取房间内玩家ID列表
        List<Integer> uids = new ArrayList<>();
        if (room.getUid1() > 0) uids.add(room.getUid1());
        if (room.getUid2() > 0 && room.getUid2() != room.getUid1()) uids.add(room.getUid2());

        // 收集玩家状态信息
        List<Map<String, Object>> playersStatus = new ArrayList<>();
        for (Integer uid : uids) {
            PlayerAgainst player = playerAgainstMap.get(uid);
            if (player != null) {
                Map<String, Object> playerInfo = new HashMap<>();
                playerInfo.put("uid", uid);
                playerInfo.put("hp", player.getHp());
                playerInfo.put("hpMax", player.getHpMax());
                playerInfo.put("shield", player.getShield());
                playerInfo.put("shieldMax", player.getShieldMax());
                playerInfo.put("wealthy", player.getWealthy());
                playerInfo.put("role", player.getRole());
                playerInfo.put("cards", player.getCards());
                playersStatus.add(playerInfo);
            }
        }

        // 构造响应
        Map<String, Object> response = new HashMap<>();
        response.put("type", "fetchall_result");
        response.put("success", true);
        response.put("roomId", roomId);
        response.put("players", playersStatus);
        response.put("room", room);

        // 记录日志
        System.out.println("【fetchall】roomId=" + roomId + " 玩家信息: " + playersStatus);

        // 广播给房间内所有用户
        for (Integer uid : uids) {
            WebSocketSession ws = userSessions.get(uid);
            if (ws != null && ws.isOpen()) {
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
        }
    }

    /**
     * 处理开启卡组（购买卡牌包）消息
     * 前端发送格式: {"type":"openCardGroups","room":{"uid":"1"}}
     * 逻辑：根据uid获取玩家，对其调用playerService.OpenCards，并打印购买前后手牌和金币信息
     *
     * @param session WebSocket会话
     * @param messageData 消息数据
     * @throws Exception 处理过程中可能发生的异常
     */
    private void handleOpenCardGroups(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String uidStr = (String) roomData.get("uid");
        int uid = Integer.parseInt(uidStr);

        PlayerAgainst playerAgainst = playerAgainstMap.get(uid);
        if (playerAgainst == null) {
            sendErrorResponse(session, "未找到该玩家信息，uid=" + uid);
            return;
        }

        // 打印购买前信息
        System.out.println("【购买前】玩家ID: " + uid +
                " 金币: " + playerAgainst.getWealthy() +
                " 手牌: " + cardListSummary(playerAgainst.getCards()));

        String resultMsg = playerService.OpenCards(playerAgainst);

        // 打印购买后信息
        System.out.println("【购买后】玩家ID: " + uid +
                " 金币: " + playerAgainst.getWealthy() +
                " 手牌: " + cardListSummary(playerAgainst.getCards()));

        // 构造响应
        Map<String, Object> response = new HashMap<>();
        response.put("type", "open_card_groups_result");
        response.put("success", resultMsg.contains("成功"));
        response.put("uid", uid);
        response.put("message", resultMsg);

        // 返回最新手牌
        response.put("cards", playerAgainst.getCards());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    /**
     * 处理合成卡牌消息
     * 接收用户请求合成三张卡牌为一张新卡牌
     *
     * @param session WebSocket会话
     * @param messageData 消息数据，格式为：
     *        {
     *        "type":"synthesize",
     *        "room":{
     *        "uid":"5",
     *        "cardA":"Aname",
     *        "cardB":"Bname",
     *        "cardC":"Cname"
     *        }
     *        }
     * @throws Exception 消息处理或发送响应时可能发生的异常
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
     * 处理弃牌消息
     * 前端发送格式: {"type":"discardCard","room":{"uid":1,"card":"cardName","money":2}}
     * 逻辑：直接给玩家加money金币，然后移除一张指定名称的卡牌（假设必然存在，不做校验），返回成功和最新卡牌与金币
     */
    private void handleDiscardCard(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String uidStr = String.valueOf(roomData.get("uid"));
        int uid = Integer.parseInt(uidStr);
        String cardName = (String) roomData.get("card");
        int money = 0;
        Object moneyObj = roomData.get("money");
        if (moneyObj instanceof Integer) {
            money = (Integer) moneyObj;
        } else if (moneyObj instanceof String) {
            money = Integer.parseInt((String) moneyObj);
        }

        PlayerAgainst playerAgainst = playerAgainstMap.get(uid);
        // 加金币
        playerService.AddCoins(playerAgainst, money);
        // 丢弃卡牌
        playerAgainst.setCards(playerService.cardService.DiscardCard(playerAgainst.getCards(), cardName));

        // 打印弃牌后信息
        System.out.println("【弃牌后】玩家ID: " + uid +
                " 金币: " + playerAgainst.getWealthy() +
                " 手牌: " + cardListSummary(playerAgainst.getCards()));

        Map<String, Object> response = new HashMap<>();
        response.put("type", "discard_card_result");
        response.put("success", true);
        response.put("message", "弃牌成功");
        response.put("uid", uid);
        response.put("wealthy", playerAgainst.getWealthy());
        response.put("cards", playerAgainst.getCards());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * 处理创建房间消息
     * 接收格式: {"type":"createRoom","room":{"uid":"1"}}
     *
     * @param session WebSocket会话
     * @param messageData 消息数据
     * @throws Exception 处理过程中可能发生的异常
     */
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

    /**
     * 处理加入房间消息
     * 用户请求加入一个已存在的游戏房间
     *
     * @param session WebSocket会话
     * @param messageData 消息数据
     * @throws Exception 处理过程中可能发生的异常
     */
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

    /**
     * 处理开始游戏消息
     * 房间人数满后，可以开始游戏
     *
     * @param session WebSocket会话
     * @param messageData 消息数据
     * @throws Exception 处理过程中可能发生的异常
     */
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

    /**
     * 处理回合开始消息
     * 游戏中每一回合开始时的处理逻辑
     *{"type":"RoundBegin","room":{"roomId":"xxx","uid":"1"}}
     * @param session WebSocket会话
     * @param messageData 消息数据
     * @throws Exception 处理过程中可能发生的异常
     */
    //调用playerService.beginService,false
    //BeginService(PlayerAgainst playerAgainst1,PlayerAgainst playerAgainst2,
    //                             List<CardBattle>listPlayer1,List<CardBattle>listPlayer2,boolean goer)
    //也就是处理好之后BeginService(playerAgainst1,playerAgainst2,null,null,false)

    //发送roomId,发送uid，均为true再执行
    private void handleRoundBeginMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
        String roomId = (String) roomData.get("roomId");
        String uidStr = (String) roomData.get("uid");
        int uid = Integer.parseInt(uidStr);

        if (!roomMap.containsKey(roomId)) {
            sendErrorResponse(session, "房间不存在：" + roomId);
            return;
        }
        Room room = roomMap.get(roomId);
        if (room.getUid1() == -1 || room.getUid2() == -1) {
            sendErrorResponse(session, "房间人数不足，无法开始回合");
            return;
        }
        PlayerAgainst playerAgainst1 = playerAgainstMap.get(room.getUid1());
        PlayerAgainst playerAgainst2 = playerAgainstMap.get(room.getUid2());
        if (playerAgainst1 == null || playerAgainst2 == null) {
            sendErrorResponse(session, "玩家信息不完整，无法开始回合");
            return;
        }

        Pair<String, Integer> beginKey = new Pair<>(roomId, room.getRoundNum());
        roundBeginSyncMap.putIfAbsent(beginKey, new HashSet<>());
        Set<Integer> beginSet = roundBeginSyncMap.get(beginKey);
        synchronized (beginSet) {
            beginSet.add(uid);
            System.out.println("【RoundBegin同步】roomId=" + roomId + " round=" + room.getRoundNum() + " 已收到玩家 " + uid + " 的回合开始信号。当前已收到: " + beginSet);
            if (beginSet.contains(room.getUid1()) && beginSet.contains(room.getUid2())) {
                System.out.println("【RoundBegin同步】roomId=" + roomId + " round=" + room.getRoundNum() + " 两位玩家均已发信号，调用BeginService。");
                playerService.BeginService(playerAgainst1, playerAgainst2, null, null, false);
                roundBeginSyncMap.remove(beginKey);

                Map<String, Object> response = new HashMap<>();
                response.put("type", "round_begin_result");
                response.put("success", true);
                response.put("roomId", roomId);
                response.put("round", room.getRoundNum());
                response.put("message", "回合正式开始");
                // 加入两个玩家的所有信息
                response.put("player1", playerAgainst1);
                response.put("player2", playerAgainst2);
                response.put("uid1",room.getUid1());
                response.put("uid2",room.getUid2());
                for (Integer puid : Arrays.asList(room.getUid1(), room.getUid2())) {
                    if (userSessions.containsKey(puid)) {
                        userSessions.get(puid).sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                    }
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "round_begin_wait");
                response.put("success", true);
                response.put("roomId", roomId);
                response.put("round", room.getRoundNum());
                response.put("message", "已收到回合开始信号，等待对手...");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }
        }
    }
    /**
     * 处理回合结束消息
     * 游戏中每一回合结束时的处理逻辑
     *
     * @param session WebSocket会话
     * @param messageData 消息数据
     * @throws Exception 处理过程中可能发生的异常
     */
    private void handleRoundEndMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");

        String roomId = (String) roomData.get("roomId");
        String uid1Str = (String) roomData.get("uid");
        int uid1 = Integer.parseInt(uid1Str);
        List<String> cardList1 = (List<String>) roomData.get("cardList1");

        if (!roomMap.containsKey(roomId)) {
            sendErrorResponse(session, "房间不存在：" + roomId);
            return;
        }

        Room room = roomMap.get(roomId);
        if (room.getUid2() == -1) {
            sendErrorResponse(session, "房间人数不足，无法进行对战");
            return;
        }

        PlayerAgainst playerAgainst1 = playerAgainstMap.get(room.getUid1());
        PlayerAgainst playerAgainst2 = playerAgainstMap.get(room.getUid2());

        if (playerAgainst1 == null || playerAgainst2 == null) {
            sendErrorResponse(session, "玩家信息不完整，无法进行对战");
            return;
        }

        Pair<String, Integer> roundKey = new Pair<>(roomId, room.getRoundNum());

        // 打印回合前状态
        System.out.println("【回合结束前】");
        System.out.println("Room: " + roomId + " Round: " + room.getRoundNum());
        System.out.println("玩家1（uid="+room.getUid1()+"）: " + playerSummary(playerAgainst1));
        System.out.println("玩家2（uid="+room.getUid2()+"）: " + playerSummary(playerAgainst2));

        if (roundEndDTOMapHistory.containsKey(roundKey)) {
            // 已有一个玩家提交了数据
            RoundEndDTO existingData = roundEndDTOMapHistory.get(roundKey);
            List<String> otherPlayerCards = existingData.getCardsName();
            int otherPlayerId = existingData.getUserId();

            List<CardBattle> listPlayer1, listPlayer2;
            if (uid1 == room.getUid1()) {
                listPlayer1 = playerService.GetList(cardList1);
                listPlayer2 = playerService.GetList(otherPlayerCards);
            } else {
                listPlayer1 = playerService.GetList(otherPlayerCards);
                listPlayer2 = playerService.GetList(cardList1);
            }

            playerService.MainService(playerAgainst1, playerAgainst2, listPlayer1, listPlayer2, room);

            // 打印回合后状态
            System.out.println("【回合结束后】");
            System.out.println("Room: " + roomId + " Round: " + room.getRoundNum());
            System.out.println("玩家1（uid="+room.getUid1()+"）: " + playerSummary(playerAgainst1));
            System.out.println("玩家2（uid="+room.getUid2()+"）: " + playerSummary(playerAgainst2));

            roundEndDTOMapHistory.remove(roundKey);

            // 判断胜负
            int hp1 = playerAgainst1.getHp();
            int hp2 = playerAgainst2.getHp();
            int winnerId = 0;
            if (hp1 <= 0 && hp2 <= 0) {
                winnerId = -1;
            } else if (hp1 <= 0) {
                winnerId = room.getUid2();
            } else if (hp2 <= 0) {
                winnerId = room.getUid1();
            } else {
                winnerId = 0;
            }

            // 广播结算结果

            Map<String, Object> response = new HashMap<>();
            response.put("type", "round_end_result");
            response.put("success", true);
            response.put("roomId", roomId);
            response.put("round", room.getRoundNum());
            response.put("message", "回合处理完成");

            for (Integer uid : Arrays.asList(room.getUid1(), room.getUid2())) {
                if (userSessions.containsKey(uid)) {
                    userSessions.get(uid).sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }
            }
            if(room.getRoundNum()%3 == 0 && room.getRoundNum()!=0)
            {
                playerService.AddShield(playerAgainst1,1);
                playerService.AddShield(playerAgainst2,1);
            }
            room.setRoundNum(room.getRoundNum()+1);

            //判断：如果playerAgainst1/2的hp小于等于0，那么对应的人输，返回赢家id
            // 如果都小于等于0，id返回-1，
            // 那么广播消息，然后return，否则不广播
            // 如果有胜负，广播赢家
            if (winnerId != 0) {
                Map<String, Object> winMsg = new HashMap<>();
                winMsg.put("type", "game_over");
                winMsg.put("room", roomMap.get(roomId));
                winMsg.put("winner_id", winnerId);
                winMsg.put("message", winnerId == -1 ? "双方同时失败，平局" : "赢家ID: " + winnerId);

                for (Integer uid : Arrays.asList(room.getUid1(), room.getUid2())) {
                    if (userSessions.containsKey(uid)) {
                        userSessions.get(uid).sendMessage(new TextMessage(objectMapper.writeValueAsString(winMsg)));
                    }
                }
                return;
            }
            playerService.BeginService(playerAgainst1, playerAgainst2, null, null, false);
            response = new HashMap<>();
            response.put("type", "round_begin_result");
            response.put("success", true);
            response.put("roomId", roomId);
            response.put("round", room.getRoundNum());
            response.put("message", "回合正式开始");
            // 加入两个玩家的所有信息
            response.put("player1", playerAgainst1);
            response.put("player2", playerAgainst2);
            response.put("uid1",room.getUid1());
            response.put("uid2",room.getUid2());
            for (Integer puid : Arrays.asList(room.getUid1(), room.getUid2())) {
                if (userSessions.containsKey(puid)) {
                    userSessions.get(puid).sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }
            }
        } else {
            RoundEndDTO newData = new RoundEndDTO();
            newData.setRoomId(roomId);
            newData.setUserId(uid1);
            newData.setCardsName(cardList1);
            newData.setRound(room.getRoundNum());
            roundEndDTOMapHistory.put(roundKey, newData);

            Map<String, Object> response = new HashMap<>();
            response.put("type", "round_end_received");
            response.put("success", true);
            response.put("roomId", roomId);
            response.put("message", "回合数据已接收，等待对手提交");

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
    }

    /**
     * 打印玩家信息摘要
     */
    private String playerSummary(PlayerAgainst player) {
        StringBuilder sb = new StringBuilder();
        sb.append("hp=").append(player.getHp()).append("/")
                .append(player.getHpMax()).append(" shield=").append(player.getShield()).append("/")
                .append(player.getShieldMax()).append(" wealthy=").append(player.getWealthy())
                .append(" role=").append(player.getRole())
                .append(" cards=").append(cardListSummary(player.getCards()));
        return sb.toString();
    }

    /**
     * 打印卡牌列表摘要
     */
    private String cardListSummary(List<CardBattle> cards) {
        if (cards == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (CardBattle cb : cards) {
            sb.append(cb.getCardName()).append("(").append(cb.getCardType()).append(",num=").append(cb.getCardNum()).append(",size=").append(cb.getCardSize()).append("), ");
        }
        if (cards.size() > 0) sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }
    /**
     * 处理获取房间状态消息
     * 客户端可以查询指定房间的当前状态信息
     *
     * @param session WebSocket会话
     * @param messageData 消息数据
     * @throws Exception 处理过程中可能发生的异常
     */
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

    /**
     * 发送错误响应给客户端
     * 当处理请求过程中出现错误时调用此方法
     *
     * @param session WebSocket会话
     * @param errorMessage 错误信息
     * @throws Exception 发送消息过程中可能发生的异常
     */
    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "error");
        response.put("success", false);
        response.put("message", errorMessage);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * 广播消息给所有连接的客户端
     * 用于需要通知所有在线用户的场景
     *
     * @param msg 要广播的消息
     */
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

    /**
     * 生成游戏状态的JSON字符串
     * 用于向客户端提供游戏总体状况信息
     *
     * @return 游戏状态的JSON字符串
     */
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
     * 创建游戏房间
     * 为用户创建一个新的游戏房间并返回房间ID
     *
     * @param userId 创建房间的用户ID
     * @return 返回新创建房间的ID
     */
    public String CreateRoom(int userId) {
        Room room=initService.CreateRoom(userId);
        String roomId=room.getRoom();
        roomMap.put(roomId,room);
        return roomId;
    }

    /**
     * 加入游戏房间
     * 用户加入指定ID的游戏房间
     * 加入成功后会更新房间状态并通知房间内所有用户
     *
     * @param roomId 要加入的房间ID
     * @param userId 加入房间的用户ID
     * @return 返回加入结果信息，格式为"结果信息#玩家数量"
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
     * 设置房间内的角色和初始化游戏状态
     *
     * @param roomId 游戏房间ID
     * @param role1 玩家1选择的角色
     * @param role2 玩家2选择的角色
     * @return 返回游戏开始结果信息
     */
    /**
     * 开始游戏
     * 设置房间内的角色和初始化游戏状态
     * 确保每个房间只初始化一次
     *
     * @param roomId 游戏房间ID
     * @param role1 玩家1选择的角色
     * @param role2 玩家2选择的角色
     * @return 返回游戏开始结果信息
     */
    public String StartGame(String roomId, String role1, String role2){
        if(roomMap.containsKey(roomId)) {
            Room room = roomMap.get(roomId);

            if(room.getUid2() == -1) return "房间人数没满！";

            // 增加检查，避免重复初始化
            if (playerAgainstMap.containsKey(room.getUid1()) || playerAgainstMap.containsKey(room.getUid2())) {
                System.out.println("房间 " + roomId + " 已经初始化过游戏，跳过重复初始化");
                return "开始成功"; // 返回成功，避免客户端误认为失败
            }

            // 正常初始化流程
            PlayerAgainst playerAgainst1 = initService.init(roomId, room.getUid1(), role1);
            PlayerAgainst playerAgainst2 = initService.init(roomId, room.getUid2(), role2);
            playerAgainstMap.put(room.getUid1(), playerAgainst1);
            playerAgainstMap.put(room.getUid2(), playerAgainst2);

            // 添加日志便于调试
            System.out.println("房间 " + roomId + " 初始化游戏成功，玩家1=" + room.getUid1() + ", 玩家2=" + room.getUid2());
            return "开始成功";
        }
        else return "没找到房间！";
    }

    /**
     * 合成卡牌
     * 玩家使用三张卡牌合成新卡牌
     *
     * @param uid 用户ID
     * @param a 卡牌A的名称
     * @param b 卡牌B的名称
     * @param c 卡牌C的名称
     */
    public void SynthesizeCards(int uid,String a,String b,String c){
        PlayerAgainst playerAgainst = playerAgainstMap.get(uid);
        if(playerAgainst != null) {
            playerService.SynthesizeABC(playerAgainst,a,b,c);
        }
    }

    /**
     * 执行一轮游戏
     * 处理一回合中的所有玩家操作和结果计算
     *
     * @param listPlayer1 玩家1选择的卡牌列表
     * @param listPlayer2 玩家2选择的卡牌列表
     * @param roomId 游戏房间ID
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
//import org.springframework.stereotype.Service;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//@Service
//public class MainService extends TextWebSocketHandler {
//
//    Map<String,Integer>roomNum=new ConcurrentHashMap<>();
//    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
//    // 添加用户ID到会话的映射
//    private static final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        System.out.println("WebSocket连接已建立，当前连接数：" + sessions.size());
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "connection");
//        response.put("message", "连接成功");
//        response.put("sessionId", session.getId());
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        try {
//            String payload = message.getPayload();
//            System.out.println("收到消息：" + payload);
//
//            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
//            String type = (String) messageData.get("type");
//
//            // 从消息中尝试获取用户ID并关联到会话
//            if (messageData.containsKey("room") && ((Map)messageData.get("room")).containsKey("uid")) {
//                String uidStr = (String) ((Map)messageData.get("room")).get("uid");
//                try {
//                    int uid = Integer.parseInt(uidStr);
//                    userSessions.put(uid, session);
//                    System.out.println("关联用户ID " + uid + " 到会话 " + session.getId());
//                } catch (NumberFormatException e) {
//                    System.err.println("无法解析用户ID: " + e.getMessage());
//                }
//            }
//
//            switch (type) {
//                case "synthesize":
//                    handleSynthesizeMessage(session, messageData);
//                    break;
//                case "createRoom":
//                    handleCreateRoomMessage(session, messageData);
//                    break;
//                case "joinRoom":
//                    handleJoinRoomMessage(session, messageData);
//                    break;
//                case "startGame":
//                    handleStartGameMessage(session, messageData);
//                    break;
//                case "RoundBegin":
//                    handleRoundBeginMessage(session,messageData);
//                    break;
//                case "RoundEnd":
//                    handleRoundEndMessage(session,messageData);
//                    break;
//                case "getRoomStatus":
//                    handleGetRoomStatusMessage(session, messageData);
//                    break;
//                default:
//                    sendErrorResponse(session, "未知的消息类型：" + type);
//            }
//
//        } catch (Exception e) {
//            System.err.println("处理消息时发生错误：" + e.getMessage());
//            e.printStackTrace();
//            sendErrorResponse(session, "消息处理失败：" + e.getMessage());
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        sessions.remove(session);
//
//        // 清理用户会话映射
//        for (Map.Entry<Integer, WebSocketSession> entry : userSessions.entrySet()) {
//            if (entry.getValue().getId().equals(session.getId())) {
//                userSessions.remove(entry.getKey());
//                System.out.println("用户 " + entry.getKey() + " 的会话已关闭");
//                break;
//            }
//        }
//
//        System.out.println("WebSocket连接已关闭，当前连接数：" + sessions.size());
//    }
//
//    /**
//     * 处理合成卡牌消息
//     * @param session WebSocket会话
//     * @param messageData 消息数据
//    {
//    "type":"synthesize",
//    "room":{
//    "uid":"5",
//    "cardA":"Aname",
//    "cardB":"Bname",
//    "cardC":"Cname"
//    }
//    }，
//     * @throws Exception 异常
//     *
//     */
//    private void handleSynthesizeMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
//        String uidStr = (String) roomData.get("uid");
//        int uid = Integer.parseInt(uidStr);
//        String cardA = (String) roomData.get("cardA");
//        String cardB = (String) roomData.get("cardB");
//        String cardC = (String) roomData.get("cardC");
//
//        SynthesizeCards(uid, cardA, cardB, cardC);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "synthesize_result");
//        response.put("success", true);
//        response.put("message", "卡牌合成成功");
//        response.put("uid", uid);
//        response.put("cards", Map.of("cardA", cardA, "cardB", cardB, "cardC", cardC));
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//    }
//
//    /**
//     * {"type":"createRoom","room":{"uid":"1"}}
//     *
//     * */
//    private void handleCreateRoomMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
//        String uidStr = (String) roomData.get("uid");
//        int uid = Integer.parseInt(uidStr);
//
//        // 关联用户ID到会话
//        userSessions.put(uid, session);
//
//        String roomId = CreateRoom(uid);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "create_room_result");
//        response.put("success", true);
//        response.put("roomId", roomId);
//        response.put("uid", uid);
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//    }
//
//    private void handleJoinRoomMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
//        String roomId = (String) roomData.get("roomId");
//        String uidStr = (String) roomData.get("uid");
//        int uid = Integer.parseInt(uidStr);
//
//        // 关联用户ID到会话
//        userSessions.put(uid, session);
//
//        String result = JoinRoom(roomId, uid);
//        boolean success = result.contains("成功");
//
//        // 解析玩家数量
//        int playerCount = 0;
//        if (success && result.contains("#")) {
//            String[] parts = result.split("#");
//            if (parts.length > 1) {
//                try {
//                    playerCount = Integer.parseInt(parts[1]);
//                } catch (NumberFormatException e) {
//                    System.err.println("解析玩家数量失败: " + e.getMessage());
//                }
//            }
//        }
//
//        // 获取房间信息
//        Room room = null;
//        if (roomMap.containsKey(roomId)) {
//            room = roomMap.get(roomId);
//
//            // 检查：如果是同一用户加入自己创建的房间，直接告知用户
//            if (room.getUid1() == uid && room.getUid2() == uid) {
//                Map<String, Object> warningMsg = new HashMap<>();
//                warningMsg.put("type", "room_warning");
//                warningMsg.put("message", "您已经在该房间中，无需重复加入");
//                warningMsg.put("roomId", roomId);
//                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(warningMsg)));
//            }
//        }
//
//        // 计算真实的不重复玩家数量
//        Set<Integer> uniqueUsers = new HashSet<>();
//        if (room != null) {
//            if (room.getUid1() > 0) uniqueUsers.add(room.getUid1());
//            if (room.getUid2() > 0) uniqueUsers.add(room.getUid2());
//        }
//        int uniquePlayerCount = uniqueUsers.size();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "join_room_result");
//        response.put("success", success);
//        response.put("message", result.split("#")[0]);
//        response.put("roomId", roomId);
//        response.put("uid", uid);
//        response.put("playerCount", uniquePlayerCount);  // 使用不重复计算的玩家数
//        response.put("isFull", uniquePlayerCount >= 2);
//        response.put("readyToStart", uniquePlayerCount >= 2);
//
//        // 添加房间内的用户信息
//        if (room != null) {
//            response.put("roomOwner", room.getUid1());
//            response.put("roomJoiner", room.getUid2());
//            response.put("uniqueUsers", new ArrayList<>(uniqueUsers));
//        }
//
//        // 发送响应
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//
//        // 如果房间已满，再次直接发送一条更新消息给当前用户
//        if (playerCount >= 2) {
//            Map<String, Object> fullRoomMsg = new HashMap<>();
//            fullRoomMsg.put("type", "room_update");
//            fullRoomMsg.put("roomId", roomId);
//            fullRoomMsg.put("playerCount", playerCount);
//            fullRoomMsg.put("isFull", true);
//            fullRoomMsg.put("readyToStart", true);
//            fullRoomMsg.put("message", "房间已满，准备开始游戏");
//
//            // 确保当前用户收到这条消息
//            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(fullRoomMsg)));
//            System.out.println("发送房间已满通知给用户: " + uid);
//        }
//    }
//
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
//    private void handleRoundBeginMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        //调用playerService.beginService,false
//        //发送roomId,发送uid，均为true再执行
//    }
//
//    private void handleRoundEndMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        //调用playerService.MainService
//        //需要：当前卡牌的英文名，当前roomId
//        //同步所有的
//    }
//
//    private void handleGetRoomStatusMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
//        Map<String, Object> roomData = (Map<String, Object>) messageData.get("room");
//        String roomId = (String) roomData.get("roomId");
//
//        if (roomMap.containsKey(roomId)) {
//            Room room = roomMap.get(roomId);
//            int playerCount = 0;
//            if (room.getUid1() > 0) playerCount++;
//            if (room.getUid2() > 0) playerCount++;
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("type", "room_status_result");
//            response.put("roomId", roomId);
//            response.put("playerCount", playerCount);
//            response.put("isFull", playerCount >= 2);
//            response.put("users", Arrays.asList(room.getUid1(), room.getUid2()));
//
//            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//
//            // 如果房间已满，同时广播一条通知消息
//            if (playerCount >= 2) {
//                Map<String, Object> updateMsg = new HashMap<>();
//                updateMsg.put("type", "room_update");
//                updateMsg.put("roomId", roomId);
//                updateMsg.put("playerCount", playerCount);
//                updateMsg.put("isFull", true);
//                updateMsg.put("readyToStart", true);
//
//                broadcast(objectMapper.writeValueAsString(updateMsg));
//            }
//        } else {
//            sendErrorResponse(session, "房间不存在：" + roomId);
//        }
//    }
//
//    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
//        Map<String, Object> response = new HashMap<>();
//        response.put("type", "error");
//        response.put("success", false);
//        response.put("message", errorMessage);
//
//        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
//    }
//
//    private void broadcast(String msg) {
//        System.out.println("广播消息: " + msg); // 添加日志便于调试
//
//        for (WebSocketSession session : sessions) {
//            try {
//                if (session.isOpen()) {
//                    session.sendMessage(new TextMessage(msg));
//                }
//            } catch (Exception e) {
//                System.err.println("广播消息失败到会话 " + session.getId() + ": " + e.getMessage());
//            }
//        }
//    }
//
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
//    //房间号-room的映射
//    Map<String, Room> roomMap = new ConcurrentHashMap<>();
//    //用户id-playerAgainst的映射
//    Map<Integer, PlayerAgainst>playerAgainstMap = new ConcurrentHashMap<>();
//    //回合数-根据pair<房间，回合数>进行查找
//    Map<Pair<String,Integer>, RoundEndDTO>roundEndDTOMapHistory=new ConcurrentHashMap<>();
//
//    PlayerService playerService=new PlayerService();
//    InitService initService=new InitService();
//
//    /**
//     * 创建房间
//     * @param userId 用户ID
//     * @return 返回房间ID
//     */
//    public String CreateRoom(int userId) {
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
//    /**
//     * 加入房间
//     * @param roomId 房间ID
//     * @param userId 用户ID
//     * @return 返回加入结果信息
//     */
//    public String JoinRoom(String roomId, int userId) {
//        if (roomMap.containsKey(roomId)) {
//            Room room = roomMap.get(roomId);
//            initService.joinRoom(room, userId);
//
//            // 修正：计算真实的不重复玩家数量
//            int playerCount = 0;
//            // 使用Set来确保不重复计算
//            Set<Integer> uniqueUsers = new HashSet<>();
//            if (room.getUid1() > 0) uniqueUsers.add(room.getUid1());
//            if (room.getUid2() > 0) uniqueUsers.add(room.getUid2());
//            playerCount = uniqueUsers.size();
//
//            roomNum.put(roomId, playerCount);
//
//            try {
//                // 创建房间更新消息
//                Map<String, Object> roomUpdateMsg = new HashMap<>();
//                roomUpdateMsg.put("type", "room_update");
//                roomUpdateMsg.put("roomId", roomId);
//                roomUpdateMsg.put("playerCount", playerCount);
//                roomUpdateMsg.put("isFull", playerCount >= 2);
//                roomUpdateMsg.put("joinedUserId", userId);
//                roomUpdateMsg.put("readyToStart", playerCount >= 2);
//                roomUpdateMsg.put("uniqueUsers", new ArrayList<>(uniqueUsers));  // 添加唯一用户列表以便调试
//
//                String updateMsg = objectMapper.writeValueAsString(roomUpdateMsg);
//
//                // 发送给房间内的所有用户
//                for (int uid : uniqueUsers) {
//                    if (userSessions.containsKey(uid)) {
//                        WebSocketSession userSession = userSessions.get(uid);
//                        if (userSession.isOpen()) {
//                            userSession.sendMessage(new TextMessage(updateMsg));
//                            System.out.println("直接发送房间更新给用户(uid=" + uid + ")");
//                        }
//                    }
//                }
//
//                // 备份通知机制
//                broadcast(updateMsg);
//
//            } catch (Exception e) {
//                System.err.println("发送房间更新失败: " + e.getMessage());
//            }
//
//            return "加入成功!#" + playerCount;
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
//        if(roomMap.containsKey(roomId)) {
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
//        if(playerAgainst != null) {
//            playerService.SynthesizeABC(playerAgainst,a,b,c);
//        }
//    }
//
//    /**
//     * 执行一轮游戏
//     * @param listPlayer1 玩家1卡牌列表
//     * @param listPlayer2 玩家2卡牌列表
//     * @param roomId 房间ID
//     */
//    public void RunARound(List<CardBattle>listPlayer1, List<CardBattle>listPlayer2, String roomId){
//        Room room=roomMap.get(roomId);
//        PlayerAgainst playerAgainst1=playerAgainstMap.get(room.getUid1());
//        PlayerAgainst playerAgainst2=playerAgainstMap.get(room.getUid2());
//        playerService.MainService(playerAgainst1, playerAgainst2, listPlayer1, listPlayer2, room);
//    }
//}
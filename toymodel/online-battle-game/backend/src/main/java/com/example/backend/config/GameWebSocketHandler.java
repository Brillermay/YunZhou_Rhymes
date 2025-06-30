package com.example.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameWebSocketHandler extends TextWebSocketHandler {
    // 简单的全局游戏状态
    private static final List<String> gameState = new ArrayList<>(Arrays.asList("", "", "", "", "", "", "", "", ""));
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        // 新连接发送当前状态
        session.sendMessage(new TextMessage(gameStateToJson()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 前端发送 {"index":0,"value":"1"} 这样的消息
        String payload = message.getPayload();
        int idx = -1;
        String value = "";
        try {
            // 简单解析，不建议生产用
            if (payload.contains("index") && payload.contains("value")) {
                idx = Integer.parseInt(payload.replaceAll(".*\"index\":(\\d+).*", "$1"));
                value = payload.replaceAll(".*\"value\":\"?([^\"]+)\"?.*", "$1");
            }
        } catch (Exception ignored) {}
        if (idx >= 0 && idx < 9) {
            gameState.set(idx, value);
            // 广播给所有人
            broadcast(gameStateToJson());
        }
        // 支持重置
        if (payload.contains("reset")) {
            for (int i = 0; i < 9; i++) gameState.set(i, "");
            broadcast(gameStateToJson());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    private void broadcast(String msg) {
        for (WebSocketSession s : sessions) {
            try {
                s.sendMessage(new TextMessage(msg));
            } catch (Exception ignored) {}
        }
    }

    private String gameStateToJson() {
        try {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("gameState", gameState);
        return mapper.writeValueAsString(map);
    } catch (Exception e) {
        return "{\"gameState\":[]}";
    }
    }
}
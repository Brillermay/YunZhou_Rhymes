package com.example.bg.GameBG.Play.Util;

import com.example.bg.GameBG.Play.Service.MainService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // 👉 关键注解：启用WebSocket支持
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 👇 注册处理器并指定连接路径
        registry.addHandler(new MainService(), "/ws/game")
                .setAllowedOrigins("*"); // ⚠️ 允许所有域名访问（生产环境需限制）
    }
}
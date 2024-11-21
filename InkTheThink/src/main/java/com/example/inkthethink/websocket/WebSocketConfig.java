package com.example.inkthethink.websocket;

import com.example.inkthethink.service.RoomService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RoomService roomService;

    public WebSocketConfig(RoomService roomService) {
        this.roomService = roomService;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(roomService), "/draw")
                .setAllowedOrigins("*");
    }
}
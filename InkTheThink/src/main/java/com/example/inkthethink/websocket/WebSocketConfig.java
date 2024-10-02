package com.example.inkthethink.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    // trial : by mais : tried to fix broadcasting from and to the phones
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new com.example.inkthethink.websocket.DrawingWebSocketHandler(), "/draw")
                .setAllowedOrigins("*"); // Allow all origins
    }
}
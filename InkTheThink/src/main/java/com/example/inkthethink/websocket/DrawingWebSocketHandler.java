package com.example.inkthethink.websocket;

import lombok.Data;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.*;
@Data
public class DrawingWebSocketHandler extends TextWebSocketHandler {

    private Map<String, Set<WebSocketSession>> rooms = new HashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.startsWith("{\"type\":\"join\"")) {
            // Extract roomId and nickname from the message and add user to the room
            String roomId = payload.split("\"roomId\":\"")[1].split("\"")[0];
            String nickname = payload.split("\"nickname\":\"")[1].split("\"")[0];
            session.getAttributes().put("roomId", roomId);
            session.getAttributes().put("nickname", nickname);
            rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(session);

        } else if (payload.startsWith("{\"type\":\"draw\"")) {
            // Handle draw events and broadcast to room
            String roomId = (String) session.getAttributes().get("roomId");
            broadcastToRoom(roomId, payload, session);

        } else if (payload.startsWith("{\"type\":\"clear\"")) {
            // Handle clear events and broadcast to room
            String roomId = (String) session.getAttributes().get("roomId");
            broadcastToRoom(roomId, payload, session);

        } else if (payload.startsWith("{\"type\":\"chat\"")) {
            // Handle chat messages and broadcast to room
            String roomId = (String) session.getAttributes().get("roomId");
            broadcastChatMessage(roomId, payload);

        } else if (payload.startsWith("{\"type\":\"gameStart\"")) {
            // Handle game start event and broadcast to room
            String roomId = (String) session.getAttributes().get("roomId");
            broadcastGameStart(roomId);
        }
    }

    // Method to broadcast a message to all users in the room
    private void broadcastToRoom(String roomId, String message, WebSocketSession senderSession) throws Exception {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (!session.equals(senderSession)) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }

    // Method to broadcast chat messages to all users in the room
    private void broadcastChatMessage(String roomId, String payload) throws Exception {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                session.sendMessage(new TextMessage(payload));
            }
        }
    }

    // Method to broadcast the "game started" event to all users in the room
    private void broadcastGameStart(String roomId) throws Exception {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions != null) {
            String gameStartMessage = "{\"type\":\"gameStart\",\"message\":\"The game has started!\"}";
            for (WebSocketSession session : sessions) {
                session.sendMessage(new TextMessage(gameStartMessage));
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established.");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        String roomId = (String) session.getAttributes().get("roomId");
        if (roomId != null) {
            Set<WebSocketSession> sessions = rooms.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    rooms.remove(roomId); // Clean up the room if empty
                }
            }
        }
    }

}

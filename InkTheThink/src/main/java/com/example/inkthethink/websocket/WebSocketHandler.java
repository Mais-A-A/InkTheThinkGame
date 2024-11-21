package com.example.inkthethink.websocket;

import com.example.inkthethink.service.RoomService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Data

public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private final RoomService roomService;
    private Map<String, Set<WebSocketSession>> rooms = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
//        System.out.println("Received: " + payload);

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.get("type").asText();

            switch (type) {
                case "join":
                    String roomId = jsonNode.get("roomId").asText();
                    String nickname = jsonNode.get("nickname").asText();
                    session.getAttributes().put("roomId", roomId);
                    session.getAttributes().put("nickname", nickname);
                    rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(session);
                    broadcastToRoom(roomId, "{\"type\":\"system\",\"message\":\"" + nickname + " has joined the room!\"}", session);
                    break;

                case "draw":
                case "clear":
                    roomId = (String) session.getAttributes().get("roomId");
                    broadcastToRoom(roomId, payload, session);
                    break;

                case "chat":
                    roomId = (String) session.getAttributes().get("roomId");
                    broadcastChatMessage(roomId, payload);
                    break;

                case "startGame":
                    roomId = (String) session.getAttributes().get("roomId");
                    roomService.startGame(roomId, getPlayerSessionsInRoom(roomId));
                    break;

                default:
                    System.out.println("Unknown message type: " + type);
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            // Optionally close the session
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    // Get the sessions of all players in the room
    private Map<String, WebSocketSession> getPlayerSessionsInRoom(String roomId) {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        Map<String, WebSocketSession> playerSessions = new HashMap<>();
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                String playerName = (String) session.getAttributes().get("nickname");
                playerSessions.put(playerName, session);
            }
        }
        System.out.println(playerSessions);
        return playerSessions;
    }

    @PostConstruct
    public void init() {
        if (roomService == null) {
            System.out.println("RoomService is not injected!");
        } else {
            System.out.println("RoomService is injected successfully.");
        }
    }


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

    // New method to update the scoreboard for all players in the room
    public void updateScoreboardForRoom(String roomId, String scoreboardMessage) {
        Map<String, WebSocketSession> playerSessions = getPlayerSessionsInRoom(roomId);
        for (WebSocketSession session : playerSessions.values()) {
            try {
                session.sendMessage(new TextMessage(scoreboardMessage));
            } catch (IOException e) {
                System.err.println("Failed to send scoreboard update to session: " + session.getId());
                e.printStackTrace();
            }
        }
    }

    private void broadcastChatMessage(String roomId, String payload) throws Exception {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                session.sendMessage(new TextMessage(payload));
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
        //System.out.println("closed");
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

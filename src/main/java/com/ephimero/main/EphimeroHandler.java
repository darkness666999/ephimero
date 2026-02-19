package com.ephimero.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EphimeroHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToId = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        switch (type) {
            case "join":
                String myId = json.get("myId").asText();
                sessions.put(myId, session);
                sessionToId.put(session.getId(), myId);
                System.out.println("Usuario registrado: " + myId);
                break;

            case "signal":
                String targetId = json.get("targetId").asText();
                WebSocketSession targetSession = sessions.get(targetId);
                if (targetSession != null && targetSession.isOpen()) {
                    targetSession.sendMessage(new TextMessage(message.getPayload()));
                } else {
                    String errorMsg = objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "Target peer not found or offline"
                ));
                session.sendMessage(new TextMessage(errorMsg));
            }
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String id = sessionToId.remove(session.getId());
        if (id != null) {
            sessions.remove(id);
            System.out.println("Usuario desconectado: " + id);
        }
    }
}
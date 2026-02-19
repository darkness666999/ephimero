package com.ephimero.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EphimeroHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    // Mapa de HashIdentidad -> Sesión
    // private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>(); //Para usar sin swagger
    public static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // Mapa inverso para limpieza rápida al desconectar
    private final Map<String, String> sessionToId = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Parseamos el mensaje JSON
        JsonNode json = objectMapper.readTree(message.getPayload());
        // El campo "type" indica el tipo de mensaje (join, signal)
        String type = json.get("type").asText();

        switch (type) {
            case "join":
                // Registro: El cliente envía su hash(teléfono(formato +568281234567)+usuario) para identificarse
                String myId = json.get("myId").asText();
                // Guardamos la sesión asociada a este ID
                sessions.put(myId, session);
                sessionToId.put(session.getId(), myId);
                System.out.println("Usuario registrado: " + myId);
                break;

            case "signal":
                // Reenvío de señal WebRTC (offer, answer, candidate)
                String targetId = json.get("targetId").asText();
                // Buscamos la sesión del destinatario
                WebSocketSession targetSession = sessions.get(targetId);
                // Si la sesión existe y está abierta, le enviamos el mensaje
                if (targetSession != null && targetSession.isOpen()) {
                    // El mensaje viene encriptado y se encripta y desencripta en el cliente, por lo que el servidor solo lo reenvía sin procesar
                    targetSession.sendMessage(new TextMessage(message.getPayload()));
                }
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Cuando un usuario se desconecta, limpiamos ambos mapas para evitar fugas de memoria
        String id = sessionToId.remove(session.getId());
        if (id != null) {
            sessions.remove(id);
            System.out.println("Usuario desconectado: " + id);
        }
    }
}
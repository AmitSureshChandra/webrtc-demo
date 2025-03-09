package com.github.AmitSureshchandra.signaling_server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebRTCSignalingServer extends TextWebSocketHandler {
    ObjectMapper objectMapper = new ObjectMapper();
    Logger logger = LoggerFactory.getLogger(WebRTCSignalingServer.class);

    private static final ConcurrentHashMap<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = session.getAttributes().get("roomId").toString();

        Map<String, WebSocketSession> roomSessions = sessions.getOrDefault(roomId, new HashMap<>());
        roomSessions.put(session.getId(), session);

        sessions.put(roomId, roomSessions);

        logger.info("roomID : {}", roomId);
        logger.info("{} connected", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received {} from {}", message, session.getId());

        String roomId = session.getAttributes().get("roomId").toString();

        // forward SDP & ice candidates to peer
        for (WebSocketSession peer : sessions.get(roomId).values()) {
            if (!peer.getId().equals(session.getId())) {
                peer.sendMessage(message);
            }
        }
    }



    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = session.getAttributes().get("roomId").toString();
        sessions.get(roomId).remove(session.getId());

        // only one session left in the room
        sessions.get(roomId).values().forEach(s -> {
            try {
                s.sendMessage(new TextMessage("{\"type\":\"end_call\"}"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        logger.info("{} closed", session.getId());
    }
}

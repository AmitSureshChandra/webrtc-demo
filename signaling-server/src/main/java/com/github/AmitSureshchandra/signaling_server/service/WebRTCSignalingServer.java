package com.github.AmitSureshchandra.signaling_server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.AmitSureshchandra.signaling_server.dto.SignalingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebRTCSignalingServer extends TextWebSocketHandler {
    ObjectMapper objectMapper = new ObjectMapper();
    Logger logger = LoggerFactory.getLogger(WebRTCSignalingServer.class);

    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        logger.info("{} connected", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received {} from {}", message, session.getId());

        // forward SDP & ice candidates to peer
        for (WebSocketSession peer : sessions.values()) {
            if (!peer.getId().equals(session.getId())) {
                peer.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        logger.info("{} closed", session.getId());
    }
}

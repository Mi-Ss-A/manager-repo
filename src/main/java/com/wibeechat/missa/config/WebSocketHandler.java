package com.wibeechat.missa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wibeechat.missa.domain.Server;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void sendServerUpdate(Server server) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String message = mapper.writeValueAsString(server);
            for (WebSocketSession session : sessions) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
        }
    }
}

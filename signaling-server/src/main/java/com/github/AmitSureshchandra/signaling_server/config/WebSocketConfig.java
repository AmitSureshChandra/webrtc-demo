package com.github.AmitSureshchandra.signaling_server.config;

import com.github.AmitSureshchandra.signaling_server.service.HttpHandshakeInterceptor;
import com.github.AmitSureshchandra.signaling_server.service.WebRTCSignalingServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebRTCSignalingServer(), "/ws").addInterceptors(new HttpHandshakeInterceptor()).setAllowedOrigins("*");
    }
}

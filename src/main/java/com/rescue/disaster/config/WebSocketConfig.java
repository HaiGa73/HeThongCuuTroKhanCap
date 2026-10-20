package com.rescue.disaster.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Topic để client (Manager/Admin/User) subscribe nhận tin
        registry.enableSimpleBroker("/topic");
        // Prefix cho các message gửi từ client lên server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm bắt tay kết nối WebSocket (hỗ trợ SockJS fallback)
        registry.addEndpoint("/ws-rescue")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
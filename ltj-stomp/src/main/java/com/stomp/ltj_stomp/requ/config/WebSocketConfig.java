package com.stomp.ltj_stomp.requ.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，配置目的地前缀
        config.enableSimpleBroker("/topic", "/queue");

        // 设置应用目的地前缀（客户端发送消息到服务器的前缀）
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户目的地前缀（用于点对点消息）
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*"); // 配置允许的源，生产环境应该指定具体域名


        // 注册STOMP端点
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")  // 配置允许的源，生产环境应该指定具体域名
                .withSockJS();  // 启用SockJS支持
    }
}
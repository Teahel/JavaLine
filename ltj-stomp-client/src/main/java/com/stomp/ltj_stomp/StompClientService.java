package com.stomp.ltj_stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Service
public class StompClientService {

    @Value("${websocket.url:ws://127.0.0.1:8080/ltj-stomp/ws}")
    private String websocketUrl;

    @Value("${websocket.username:ltj}")
    private String username;

    @Value("${websocket.password:123456}")
    private String password;

    private ProductionStompClient stompClient;

    @PostConstruct
    public void init() {
        // 创建客户端
        stompClient = new ProductionStompClient(websocketUrl, username, password);

        // 设置消息处理器
        stompClient.onPublicMessage(this::handlePublicMessage);
        stompClient.onPrivateMessage(this::handlePrivateMessage);

        // 连接服务器
        stompClient.connect();

        log.info("STOMP客户端服务初始化完成");
    }

    /**
     * 处理公共消息
     */
    private void handlePublicMessage(MessageDTO message) {
        log.info("收到公共消息: {}", message);
        // 处理业务逻辑...
    }

    /**
     * 处理私聊消息
     */
    private void handlePrivateMessage(MessageDTO message) {
        log.info("收到私聊消息: {}", message);
        // 处理业务逻辑...
    }

    /**
     * 发送通知
     */
    public void sendNotification(String content) {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.sendPublicMessage(content);
            log.info("通知已发送: {}", content);
        } else {
            log.warn("STOMP客户端未连接，消息发送失败");
        }
    }

    /**
     * 发送告警
     */
    public void sendAlert(String content) {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.sendPublicMessage("[ALERT] " + content);
        }
    }

    @PreDestroy
    public void destroy() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}

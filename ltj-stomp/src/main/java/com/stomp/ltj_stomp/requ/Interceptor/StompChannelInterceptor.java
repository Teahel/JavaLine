package com.stomp.ltj_stomp.requ.Interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompChannelInterceptor implements ChannelInterceptor {

    /**
     * 消息发送前处理
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            String sessionId = accessor.getSessionId();

            log.debug("STOMP命令: {}, sessionId: {}", command, sessionId);

            // 可以在这里做权限验证
            if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                log.info("订阅请求 - destination: {}", destination);

                // 检查是否有权限订阅
                if (!hasPermission(accessor, destination)) {
                    log.warn("无权限订阅: {}", destination);
                    return null;
                }
            }

            // 连接时设置用户信息
            if (StompCommand.CONNECT.equals(command)) {
                String username = accessor.getLogin();
                String passcode = accessor.getPasscode();
                log.info("连接请求 - username: {}", username);

                // 验证用户
                if (authenticate(username, passcode)) {
                    // 可以设置用户信息到session
                    accessor.setUser(() -> username);
                } else {
                    log.warn("认证失败: {}", username);
                    return null;
                }
            }
        }

        return message;
    }

    /**
     * 消息发送后处理
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor != null && accessor.getCommand() != null) {
            log.debug("消息已发送 - command: {}, sent: {}", accessor.getCommand(), sent);
        }
    }

    /**
     * 发送完成后处理
     */
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel,
                                    boolean sent, Exception ex) {
        if (ex != null) {
            log.error("消息发送失败", ex);
        }
    }

    private boolean hasPermission(StompHeaderAccessor accessor, String destination) {
        // 实现权限检查逻辑
        return true;
    }

    private boolean authenticate(String username, String passcode) {
        // 实现认证逻辑
        return true;
    }
}
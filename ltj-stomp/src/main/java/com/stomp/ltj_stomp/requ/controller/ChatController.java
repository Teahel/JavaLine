package com.stomp.ltj_stomp.requ.controller;



import com.stomp.ltj_stomp.requ.dto.ChatMessage;
import com.stomp.ltj_stomp.requ.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SimpMessageSendingOperations messagingOperations;

    /*@Autowired
    private MessageService messageService;*/

    // 在线用户管理
    private static final Map<String, UserInfo> onlineUsers = new ConcurrentHashMap<>();

    /**
     * 1. 处理用户连接
     * 客户端发送：CONNECT
     */
    @MessageMapping("/connect")
    public void handleConnect(Principal principal, StompHeaderAccessor headerAccessor) {
        String username = principal != null ? principal.getName() : "anonymous";
        String sessionId = headerAccessor.getSessionId();

        log.info("用户连接 - 用户名: {}, sessionId: {}", username, sessionId);

        // 存储用户信息
        UserInfo userInfo = new UserInfo(username, sessionId);
        onlineUsers.put(sessionId, userInfo);
        headerAccessor.getSessionAttributes().put("username", username);

        // 广播用户上线通知
        ChatMessage onlineMessage = new ChatMessage("SYSTEM", username + " 上线了", "系统");
        messagingTemplate.convertAndSend("/topic/user.status", onlineMessage);

        // 发送在线用户列表给所有用户
        sendOnlineUsers();
    }

    /**
     * 2. 处理用户断开连接
     * 通过事件监听器处理
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            log.info("用户断开 - 用户名: {}, sessionId: {}", username, sessionId);

            // 移除在线用户
            onlineUsers.remove(sessionId);

            // 广播用户下线通知
            ChatMessage offlineMessage = new ChatMessage("SYSTEM", username + " 下线了", "系统");
            messagingTemplate.convertAndSend("/topic/user.status", offlineMessage);

            // 更新在线用户列表
            sendOnlineUsers();
        }
    }

    /**
     * 3. 处理发送消息（广播）
     * 客户端发送到：/app/chat.sendMessage
     * 广播到：/topic/public
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
                                   Principal principal) {
        log.info("收到广播消息: {}", chatMessage);

        // 设置发送时间
        chatMessage.setTimestamp(System.currentTimeMillis());

        // 保存到数据库 todo
        //messageService.saveMessage(chatMessage);

        return chatMessage;
    }

    /**
     * 4. 处理发送消息（带返回值，不广播）
     * 客户端发送到：/app/chat.process
     * 响应发送到：/topic/processed
     */
    @MessageMapping("/chat.process")
    @SendTo("/topic/processed")
    public String processMessage(@Payload String message) {
        log.info("处理消息: {}", message);
        return "服务器已处理: " + message.toUpperCase();
    }

    /**
     * 5. 处理私聊消息
     * 客户端发送到：/app/chat.private
     */
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        log.info("收到私聊消息: {}", chatMessage);

        String toUser = chatMessage.getReceiver();
        if (toUser == null || toUser.isEmpty()) {
            log.warn("私聊消息没有指定接收者");
            return;
        }

        // 设置时间戳
        chatMessage.setTimestamp(System.currentTimeMillis());

        // 发送给指定用户
        messagingTemplate.convertAndSendToUser(
                toUser,                 // 接收者用户名
                "/queue/private",       // 目的地
                chatMessage             // 消息内容
        );

        // 同时保存到数据库 todo
        //messageService.saveMessage(chatMessage);

        log.info("私聊消息已发送给: {}", toUser);
    }

    /**
     * 6. 处理群组消息
     * 客户端发送到：/app/chat.group
     */
    @MessageMapping("/chat.group")
    public void sendGroupMessage(@Payload ChatMessage chatMessage,
                                 @Header("group-id") String groupId) {
        log.info("收到群组消息, groupId: {}, message: {}", groupId, chatMessage);

        chatMessage.setTimestamp(System.currentTimeMillis());

        // 发送到指定群组
        messagingTemplate.convertAndSend("/topic/group." + groupId, chatMessage);
    }

    /**
     * 7. 处理用户加入聊天室
     * 客户端发送到：/app/chat.addUser
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        log.info("用户加入: {}", chatMessage.getSender());

        // 将用户名添加到session中
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // 更新在线用户列表
        String sessionId = headerAccessor.getSessionId();
        onlineUsers.put(sessionId, new UserInfo(chatMessage.getSender(), sessionId));

        // 返回加入消息
        chatMessage.setType("JOIN");
        chatMessage.setTimestamp(System.currentTimeMillis());

        return chatMessage;
    }

    /**
     * 8. 处理心跳响应
     * 客户端发送到：/app/heartbeat
     */
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(Principal principal) {
        log.debug("收到心跳 from: {}", principal != null ? principal.getName() : "unknown");
        // 可以在这里更新用户最后活动时间
    }

    /**
     * 9. 订阅映射（当客户端订阅特定主题时触发）
     * 客户端订阅：/topic/initial
     */
    @SubscribeMapping("/topic.initial")
    public String handleSubscription() {
        log.info("客户端订阅了初始主题");
        return "订阅成功，欢迎！";
    }

    /**
     * 10. 订阅带参数的映射
     */
    @SubscribeMapping("/user.{username}.queue")
    public String handleUserSubscription(@DestinationVariable String username) {
        log.info("用户 {} 订阅了私有队列", username);
        return "私有队列订阅成功";
    }

    /**
     * 11. 获取在线用户列表
     * 客户端发送到：/app/users.online
     * 响应发送到：/topic/users.online
     */
    @MessageMapping("/users.online")
    @SendTo("/topic/users.online")
    public Map<String, UserInfo> getOnlineUsers() {
        log.info("获取在线用户列表，当前在线: {} 人", onlineUsers.size());
        return onlineUsers;
    }

    /**
     * 12. 处理消息回执（确认消息已接收）
     * 客户端发送到：/app/message.ack
     */
    @MessageMapping("/message.ack")
    public void handleMessageAck(@Payload String messageId,
                                 @Header("status") String status) {
        log.info("消息回执 - messageId: {}, status: {}", messageId, status);
        // 更新消息状态 todo
     //   messageService.updateMessageStatus(messageId, status);
    }

    /**
     * 13. 带异常处理的消息处理
     */
    @MessageMapping("/chat.withException")
    @SendTo("/topic/result")
    public String handleWithException(@Payload String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }
        return "处理成功: " + message;
    }

    /**
     * 14. 处理文件上传完成通知
     */
    @MessageMapping("/file.uploaded")
    public void handleFileUploaded(@Payload Map<String, Object> fileInfo,
                                   @Header("file-id") String fileId) {
        log.info("文件上传完成: {}", fileInfo);

        // 通知相关人员
        ChatMessage notification = new ChatMessage(
                "FILE",
                "文件上传完成: " + fileInfo.get("fileName"),
                "系统"
        );
        messagingTemplate.convertAndSend("/topic/file.notifications", notification);
    }

    /**
     * 15. 发送私聊消息（另一种方式）
     */
    public void sendPrivateMessage(String username, ChatMessage message) {
        messagingOperations.convertAndSendToUser(
                username,
                "/queue/messages",
                message
        );
    }

    /**
     * 16. 广播消息（服务端主动推送）
     */
    public void broadcastMessage(ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/broadcast", message);
    }

    /**
     * 17. 发送在线用户列表
     */
    private void sendOnlineUsers() {
        messagingTemplate.convertAndSend("/topic/users", onlineUsers.values());
    }

    /**
     * 18. 处理异常
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        log.error("处理消息时发生异常", exception);
        return "发生错误: " + exception.getMessage();
    }
}
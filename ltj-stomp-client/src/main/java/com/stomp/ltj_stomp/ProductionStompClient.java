package com.stomp.ltj_stomp;


import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class ProductionStompClient {

    private final String url;
    private final String username;
    private final String password;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    // 消息处理器
    private Consumer<MessageDTO> publicMessageHandler;
    private Consumer<MessageDTO> privateMessageHandler;

    // 连接状态
    private volatile boolean connected = false;
    private volatile boolean connecting = false;
    private final Object connectLock = new Object();

    // 重连配置
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private int reconnectAttempts = 0;
    private ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    public ProductionStompClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        init();
    }

    /**
     * 初始化客户端
     */
    private void init() {
        // 创建传输层
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient webSocketClient = new SockJsClient(transports);

        // 创建STOMP客户端
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setDefaultHeartbeat(new long[]{10000, 10000});

        // 设置任务调度器
        taskScheduler.initialize();
        stompClient.setTaskScheduler(taskScheduler);

        log.info("STOMP客户端初始化完成");
    }

    /**
     * 连接服务器
     */
    public void connect() {
        synchronized (connectLock) {
            if (connected || connecting) {
                return;
            }
            connecting = true;
        }

        try {
            WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
            StompHeaders connectHeaders = new StompHeaders();
            connectHeaders.setLogin(username);
            connectHeaders.setPasscode(password);
            connectHeaders.setHeartbeat(new long[]{10000, 10000});

            log.info("正在连接STOMP服务器: {}", url);

            ListenableFuture<StompSession> future = stompClient.connect(
                    url,
                    handshakeHeaders,
                    connectHeaders,
                    new SessionHandler()
            );

            // 设置连接超时
            session = future.get(10, TimeUnit.SECONDS);

            synchronized (connectLock) {
                connected = true;
                connecting = false;
                reconnectAttempts = 0;
            }

            log.info("STOMP连接成功, sessionId: {}", session.getSessionId());

            // 订阅主题
            subscribeTopics();

            // 发送上线消息
            sendOnlineMessage();

        } catch (Exception e) {
            synchronized (connectLock) {
                connected = false;
                connecting = false;
            }
            log.error("STOMP连接失败: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    /**
     * 会话处理器
     */
    private class SessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.info("STOMP会话已建立, headers: {}", connectedHeaders);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            log.error("传输错误: {}", exception.getMessage());
            handleDisconnect();
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            log.error("STOMP异常: {}", exception.getMessage());
        }
    }

    /**
     * 处理断开连接
     */
    private void handleDisconnect() {
        synchronized (connectLock) {
            connected = false;
            connecting = false;
        }
        scheduleReconnect();
    }

    /**
     * 订阅主题
     */
    private void subscribeTopics() {
        if (session == null || !session.isConnected()) {
            return;
        }

        // 订阅公共主题
        session.subscribe("/topic/public", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (publicMessageHandler != null) {
                    publicMessageHandler.accept((MessageDTO) payload);
                }
            }
        });

        // 订阅私聊主题
        session.subscribe("/user/queue/private", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (privateMessageHandler != null) {
                    privateMessageHandler.accept((MessageDTO) payload);
                }
            }
        });

        log.info("主题订阅完成");
    }

    /**
     * 发送上线消息
     */
    private void sendOnlineMessage() {
        send("/app/chat.addUser", new MessageDTO("JOIN", username + " 上线", username));
    }

    /**
     * 发送公共消息
     */
    public void sendPublicMessage(String content) {
        send("/app/chat.sendMessage", new MessageDTO("CHAT", content, username));
    }

    /**
     * 发送私聊消息
     */
    public void sendPrivateMessage(String toUser, String content) {
        send("/app/chat.private", new MessageDTO("PRIVATE", content, username));
    }

    /**
     * 通用发送方法
     */
    private void send(String destination, Object payload) {
        if (!checkConnection()) {
            return;
        }

        try {
            session.send(destination, payload);
            log.debug("消息发送成功: {}", destination);
        } catch (Exception e) {
            log.error("消息发送失败: {}", e.getMessage());
        }
    }

    /**
     * 检查连接状态
     */
    private boolean checkConnection() {
        if (session == null || !session.isConnected()) {
            log.warn("连接已断开，尝试重连");
            connect();
            return false;
        }
        return true;
    }

    /**
     * 计划重连
     */
    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            log.error("达到最大重连次数，停止重连");
            return;
        }

        reconnectAttempts++;
        long delay = Math.min(1000L * reconnectAttempts, 30000L);

        log.info("计划第{}次重连，{}ms后执行", reconnectAttempts, delay);

        reconnectExecutor.schedule(() -> {
            log.info("执行第{}次重连", reconnectAttempts);
            connect();
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 设置公共消息处理器
     */
    public void onPublicMessage(Consumer<MessageDTO> handler) {
        this.publicMessageHandler = handler;
    }

    /**
     * 设置私聊消息处理器
     */
    public void onPrivateMessage(Consumer<MessageDTO> handler) {
        this.privateMessageHandler = handler;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        synchronized (connectLock) {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }

            if (taskScheduler != null) {
                taskScheduler.shutdown();
            }

            if (reconnectExecutor != null) {
                reconnectExecutor.shutdown();
            }

            connected = false;
            connecting = false;
        }

        log.info("STOMP连接已关闭");
    }

    /**
     * 获取连接状态
     */
    public boolean isConnected() {
        return connected && session != null && session.isConnected();
    }
}
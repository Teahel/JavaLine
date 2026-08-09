package com.stomp.ltj_stomp.requ.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天消息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // 忽略null字段
public class ChatMessage {

    // 消息唯一ID
    private String id;

    // 消息类型：CHAT, JOIN, LEAVE, PRIVATE, SYSTEM, FILE, IMAGE, VIDEO, AUDIO, NOTICE, CUSTOM
    private String type;

    // 消息子类型（用于扩展）
    private String subType;

    // 消息内容
    private String content;

    // 发送者
    private String sender;

    // 发送者昵称
    private String senderName;

    // 发送者头像
    private String senderAvatar;

    // 接收者（私聊时使用）
    private String receiver;

    // 接收者昵称
    private String receiverName;

    // 聊天室ID
    private String roomId;

    // 聊天室名称
    private String roomName;

    // 时间戳（毫秒）
    private Long timestamp;

    // 格式化时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    // 消息状态：SENT, DELIVERED, READ, FAILED, RECALLED
    private String status;

    // 消息优先级：HIGH, NORMAL, LOW
    private String priority = "NORMAL";

    // 是否已读
    private Boolean isRead = false;

    // 是否已送达
    private Boolean isDelivered = false;

    // 是否需要回执
    private Boolean needReceipt = false;

    // 消息来源：WEB, APP, DESKTOP, SYSTEM
    private String source;

    // 客户端IP
    private String clientIp;

    // 设备信息
    private String deviceInfo;

    // 引用消息ID（回复时使用）
    private String replyToId;

    // 引用消息内容
    private ChatMessage replyTo;

    // 扩展属性（用于自定义数据）
    private Map<String, Object> extras = new HashMap<>();

    // 消息长度
    private Integer contentLength;

    // 是否系统消息
    private Boolean isSystem = false;

    // 是否紧急消息
    private Boolean isUrgent = false;

    // 是否静默消息（不提示）
    private Boolean isSilent = false;

    // 构造函数 - 基础消息
    public ChatMessage(String type, String content, String sender) {
        this.id = generateId();
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
        this.time = new Date(this.timestamp);
        this.status = "SENT";
        this.contentLength = content != null ? content.length() : 0;
    }

    // 构造函数 - 带接收者
    public ChatMessage(String type, String content, String sender, String receiver) {
        this(type, content, sender);
        this.receiver = receiver;
        this.type = "PRIVATE";
    }

    // 构造函数 - 完整消息
    public ChatMessage(String id, String type, String content, String sender,
                       String receiver, Long timestamp, String status) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.time = new Date(timestamp);
        this.status = status;
    }

    /**
     * 生成消息ID
     */
    private String generateId() {
        return "MSG_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 设置时间戳并自动更新time字段
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        if (timestamp != null) {
            this.time = new Date(timestamp);
        }
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.status = "READ";
    }

    /**
     * 标记为已送达
     */
    public void markAsDelivered() {
        this.isDelivered = true;
        if ("SENT".equals(this.status)) {
            this.status = "DELIVERED";
        }
    }

    /**
     * 添加扩展属性
     */
    public void addExtra(String key, Object value) {
        if (this.extras == null) {
            this.extras = new HashMap<>();
        }
        this.extras.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    public Object getExtra(String key) {
        return this.extras != null ? this.extras.get(key) : null;
    }

    /**
     * 获取扩展属性（带默认值）
     */
    public Object getExtra(String key, Object defaultValue) {
        Object value = getExtra(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 验证消息是否有效
     */
    @JsonIgnore
    public boolean isValid() {
        return content != null && !content.trim().isEmpty() &&
                sender != null && !sender.trim().isEmpty();
    }

    /**
     * 获取消息预览（截取前50个字符）
     */
    @JsonIgnore
    public String getPreview() {
        if (content == null) return "";
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    /**
     * 转换为系统消息
     */
    public ChatMessage toSystemMessage() {
        this.type = "SYSTEM";
        this.isSystem = true;
        this.sender = "SYSTEM";
        this.senderName = "系统通知";
        return this;
    }

    @Override
    public String toString() {
        return String.format("ChatMessage{id='%s', type='%s', sender='%s', receiver='%s', content='%s', timestamp=%d}",
                id, type, sender, receiver, getPreview(), timestamp);
    }
}
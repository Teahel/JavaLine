package com.stomp.ltj_stomp.requ.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private String username;           // 用户名
    private String sessionId;          // WebSocket会话ID
    private String nickname;            // 昵称
    private String avatar;              // 头像URL
    private String status;              // 状态：ONLINE, AWAY, BUSY, OFFLINE
    private String clientType;          // 客户端类型：WEB, APP, DESKTOP
    private String ipAddress;           // IP地址
    private Date loginTime;             // 登录时间
    private Date lastHeartbeat;         // 最后心跳时间
    private String currentRoomId;       // 当前所在的聊天室
    private Map<String, Object> attributes;  // 扩展属性

    // 构造函数 - 最小化
    public UserInfo(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
        this.nickname = username;        // 默认昵称等于用户名
        this.status = "ONLINE";
        this.loginTime = new Date();
        this.lastHeartbeat = new Date();
        this.attributes = new ConcurrentHashMap<>();
    }

    // 带昵称的构造函数
    public UserInfo(String username, String sessionId, String nickname) {
        this(username, sessionId);
        this.nickname = nickname;
    }

    // 完整构造函数
    public UserInfo(String username, String sessionId, String nickname, String clientType, String ipAddress) {
        this(username, sessionId, nickname);
        this.clientType = clientType;
        this.ipAddress = ipAddress;
    }

    // 更新心跳时间
    public void updateHeartbeat() {
        this.lastHeartbeat = new Date();
    }

    // 判断是否在线（心跳超时判断，默认30秒）
    public boolean isOnline() {
        if (lastHeartbeat == null) return false;
        long diff = System.currentTimeMillis() - lastHeartbeat.getTime();
        return diff < 30000; // 30秒内的心跳算在线
    }

    // 加入房间
    public void joinRoom(String roomId) {
        this.currentRoomId = roomId;
    }

    // 离开房间
    public void leaveRoom() {
        this.currentRoomId = null;
    }

    // 添加属性
    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    // 获取属性
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    // 获取在线时长（毫秒）
    public long getOnlineDuration() {
        if (loginTime == null) return 0;
        return System.currentTimeMillis() - loginTime.getTime();
    }

    // 格式化输出
    @Override
    public String toString() {
        return String.format("UserInfo{username='%s', nickname='%s', status='%s', sessionId='%s'}",
                username, nickname, status, sessionId);
    }
}
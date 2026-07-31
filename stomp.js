// 根据环境动态获取WebSocket URL
function getWebSocketUrl() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    // 如果应用有上下文路径，需要包含
    const contextPath = '/ltj-stomp'; // 根据实际情况调整
    return `${protocol}//${host}${contextPath}/ws`;
}

// 或者使用SockJS
function connect() {
    // 方式1：使用完整URL
    // const socket = new SockJS('http://127.0.0.1:8080/ltj-stomp/ws');
    
    // 方式2：使用相对路径（推荐）
    const socket = new SockJS('/ltj-stomp/ws');
    
    stompClient = Stomp.over(socket);
    
    // 禁用调试日志（可选）
    stompClient.debug = null;
    
    stompClient.connect({}, 
        function(frame) {
            console.log('连接成功:', frame);
            
            // 订阅公共主题
            stompClient.subscribe('/topic/public', function(message) {
                console.log('收到公共消息:', JSON.parse(message.body));
            });
            
            // 可以在这里发送一个测试消息
            stompClient.send("/app/test", {}, JSON.stringify({
                sender: "system",
                content: "测试连接",
                type: "TEST"
            }));
        },
        function(error) {
            console.error('连接失败:', error);
            // 5秒后重试
            setTimeout(connect, 5000);
        }
    );
}

// 页面加载时自动连接
window.onload = function() {
    connect();
};
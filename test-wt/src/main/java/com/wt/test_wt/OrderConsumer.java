package com.wt.test_wt;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receive(String orderId) {
        System.out.println("收到订单消息：" + orderId);
    }
}
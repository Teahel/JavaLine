package com.example.rabbitmq.Fanout;

import com.example.rabbitmq.RabbitmqApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class FanoutProducer extends RabbitmqApplicationTests {

    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Test
    public void sendMessage() {
        System.out.println("\n");
        System.out.println("FanoutProducer:发送消息");
        System.out.println("\n");
        rabbitMessagingTemplate.convertAndSend("fanout.exchange","","fanout,I do this!");
    }

}

package com.example.rabbitmq.direct;

import com.example.rabbitmq.RabbitmqApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class RabbitmqProducer extends RabbitmqApplicationTests {

    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Test
    public void sendMessage(){
        System.out.println("\n");
        System.out.println("directProducer:发送消息");
        System.out.println("\n");
        rabbitMessagingTemplate.convertAndSend("test_queue","I do this!");
    }
}

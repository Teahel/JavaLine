package com.example.rabbitmq.topic;

import com.example.rabbitmq.RabbitmqApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class TopicProducer extends RabbitmqApplicationTests {

    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Test
    public void sendMessage() {
        String msg = "Topic,I do this!";
        System.out.println("\n");
        System.out.println("TopicProducer发送消息: "+msg);
        System.out.println("\n");
        rabbitMessagingTemplate.convertAndSend("topic.exchange","topic",msg);

    }
}

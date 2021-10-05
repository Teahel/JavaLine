package com.example.rabbitmq.topic;

import com.example.rabbitmq.RabbitmqApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class TopicProducer2 extends RabbitmqApplicationTests {

    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Test
    public void sendMessage() {
        rabbitMessagingTemplate.convertAndSend("topic.exchange","topic.test","Topic,I use topic# flag!");
    }
}

package com.example.rabbitmq.topic;

import com.example.rabbitmq.RabbitmqApplicationTests;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RabbitListener(queues = "test_topic_queue2")
public class TopicConsumer2 extends RabbitmqApplicationTests {

    @RabbitHandler
    public void handleMessage(String msg) {
        System.out.println("\n");
        System.out.println("TopicConsumer test_topic_queue2 处理带标致#符号消息："+msg);
    }
}

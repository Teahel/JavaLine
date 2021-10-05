package com.example.rabbitmq.Fanout;

import com.example.rabbitmq.RabbitmqApplicationTests;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RabbitListener(queues = "test_fanout_queue")
public class FanoutConsumer extends RabbitmqApplicationTests {


    @RabbitHandler
    public void handleMessage(String msg) {
        System.out.println("\n");
        System.out.println("FanoutConsumer2处理消息："+msg);
        System.out.println(msg);
    }

}

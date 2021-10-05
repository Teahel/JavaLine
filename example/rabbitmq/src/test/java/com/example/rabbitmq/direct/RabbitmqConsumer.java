package com.example.rabbitmq.direct;



import com.example.rabbitmq.RabbitmqApplicationTests;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RabbitListener(queues="test_queue")
public class RabbitmqConsumer extends RabbitmqApplicationTests {

    @RabbitHandler
    public void handleMessage(String msg) {
        System.out.println("\n");
        System.out.println("directConsumer处理消息："+msg);
    }

}

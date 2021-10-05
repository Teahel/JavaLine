### 1.架构图和概念

#### 1.1 Rabbitmq 概念逻辑

Rabbitmq 类似菜鸟驿站流程，有人寄来东西，然后暂存之后，发给收件方。生产者提供消息，消费者消费信息。
下方是一个整体模型结构。
![rabbitmq.jpg](https://github.com/Teahel/JavaLine/blob/main/image/rabbitmq.jpg)

### 2、三种方式

#### 1、direct 方式

* rabbitmq自带一个没有名字默认的交换器，名字叫做amqp default.也可以自己创建选择类型
* 程序接口调用时，交换机名称字段使用："" 既可以默认使用。
* 该模式下消息传递时需要指定队列名称
* 如果 vhost 中不存在 RouteKey 中指定的队列名，则该消息会被抛弃。

![rabbitmq_direct.jpg](https://github.com/Teahel/JavaLine/blob/main/image/rabbitmq_direct.png)


具体代码案例如下

* pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.5.5</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.example</groupId>
	<artifactId>rabbitmq</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>rabbitmq</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>1.8</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-amqp</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.amqp</groupId>
			<artifactId>spring-rabbit-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>

```

* RabbitmqProducer

```
public class RabbitmqProducer extends RabbitmqApplicationTests {
    
    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Test
    public void sendMessage(){
        System.out.println("\n");
        System.out.println("directProducer:发送消息");
        System.out.println("\n");
        //指定队列名称
        rabbitMessagingTemplate.convertAndSend("test_queue","I do this!");
    }
}

```

* RabbitmqConsumer
```
//监听队列
@RabbitListener(queues="test_queue")
public class RabbitmqConsumer extends RabbitmqApplicationTests {

    @RabbitHandler
    public void handleMessage(String msg) {
        System.out.println("\n");
        System.out.println("directConsumer处理消息："+msg);
    }


```
#### 2、fanout 模式

* 不针对指定的routingkey,既不针对特定队列。若是与fanout exchange队列绑定的所有队列都能收到消息。
* 若是队列没有绑定队列则消息被丢弃
![rabbitmq_fanout.jpg](https://github.com/Teahel/JavaLine/blob/main/image/rabbitmq_fanout.jpg)






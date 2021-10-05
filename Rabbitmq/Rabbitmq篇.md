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



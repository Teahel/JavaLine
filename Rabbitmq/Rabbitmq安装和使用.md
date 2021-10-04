### Rabbitmq安装

#### 使用docker 安装

1  查看MQ是否存在

```
docker search rabbitmq:management
```
2 将镜像下载下来

```
    docker pull rabbitmq:management
```
3 发布docker镜像

```
docker run -d --name rabbit -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin -p 15672:15672 -p 5672:5672 -p 25672:25672 -p 61613:61613 -p 1883:1883 rabbitmq:management

命令中的【RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin】是web管理平台的用户名和密码

【 -p 15672:15672】 是控制平台docker映射到系统的对应端口

【 -p 5672:5672】 是应用程序的访问端口

```
4 web端的管理界面：


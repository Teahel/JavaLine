### Docker 安装 Redis

#### 1、取最新版的 Redis 镜像
```
$ docker pull redis:latest
```

#### 2、查看本地镜像

```
$ docker images
```
```
REPOSITORY    TAG       IMAGE ID       CREATED        SIZE
redis         latest    02c7f2054405   11 days ago    105MB
hello-world   latest    d1165f221234   6 months ago   13.3kB

```

#### 3、运行容器

安装完成后，我们可以使用以下命令来运行 redis 容器：
```
$ docker run -itd --name redis-test -p 6379:6379 redis
```
参数说明：

   *  -p 6379:6379：映射容器服务的 6379 端口到宿主机的 6379 端口。外部可以直接通过宿主机ip:6379 访问到 Redis 的服务。

### 4、安装成功

最后我们可以通过 docker ps 命令查看容器的运行信息：
```
CONTAINER ID   IMAGE     COMMAND                  CREATED          STATUS          PORTS                                       NAMES
2c355f7b1a99   redis     "docker-entrypoint.s…"   39 minutes ago   Up 39 minutes   0.0.0.0:6379->6379/tcp, :::6379->6379/tcp   redis-test

```
接着通过 redis-cli 连接测试使用 redis 服务。

```
docker exec -it redis-test /bin/bash                                                                                            127 ✘  21s  
root@2c355f7b1a99:/data# redis-cli
127.0.0.1:6379> set a "121"
OK
127.0.0.1:6379> get a
"121"
127.0.0.1:6379> 

```



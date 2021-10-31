### 1、拉取镜像
最新版
```
$ docker pull mysql:latest
```
指定版本

```
docker pull mysql:5.7
```

### 运行容器
```
 docker run -itd --name mysql-test -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql
```

* 参数说明：

   *  -p 3306:3306 ：映射容器服务的 3306 端口到宿主机的 3306 端口，外部主机可以直接通过 宿主机ip:3306 访问到 MySQL 的服务。
   * MYSQL_ROOT_PASSWORD=123456：设置 MySQL 服务 root 用户的密码。 
   * -v /opt/docker_v/mysql/conf:/etc/mysql/conf.d：将主机/opt/docker_v/mysql/conf目录挂载到容器的/etc/mysql/conf.d
   * -e MYSQL_ROOT_PASSWORD=123456：初始化root用户的密码
   * -d: 后台运行容器，并返回容器ID
   * imageID: mysql镜像ID


### 命令操作
```
#进入容器
docker exec -it 964e73946bc3 bash

#登录mysql
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Lzslov123!';

#添加远程登录用户
CREATE USER 'liaozesong'@'%' IDENTIFIED WITH mysql_native_password BY 'Lzslov123!';
GRANT ALL PRIVILEGES ON *.* TO 'liaozesong'@'%';

flush privileges;
```

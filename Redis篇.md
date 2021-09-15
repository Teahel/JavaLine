#### Redis

#### Redis 单线程模型详解

* “多路复用”也可被称为“复用”。多路复用通常表示在一个信道上传输多路信号或数据流的过程和技术。因为多路复用能够将多个低速信道集成到一个高速信道进行传输，从而有效地利用了高速信道。通过使用多路复用，通信运营商可以避免维护多条线路，从而有效地节约运营成本[1]。

知乎的一个对多路复用的理解：
```
作者：柴小喵
链接：https://www.zhihu.com/question/28594409/answer/52835876
```
下面举一个例子，模拟一个tcp服务器处理30个客户socket。假设你是一个老师，让30个学生解答一道题目，然后检查学生做的是否正确，你有下面几个选择：1. 第一种选择：按顺序逐个检查，先检查A，然后是B，之后是C、D。。。这中间如果有一个学生卡主，全班都会被耽误。这种模式就好比，你用循环挨个处理socket，根本不具有并发能力。2. 第二种选择：你创建30个分身，每个分身检查一个学生的答案是否正确。 这种类似于为每一个用户创建一个进程或者线程处理连接。3. 第三种选择，你站在讲台上等，谁解答完谁举手。这时C、D举手，表示他们解答问题完毕，你下去依次检查C、D的答案，然后继续回到讲台上等。此时E、A又举手，然后去处理E和A。。。 这种就是IO复用模型，Linux下的select、poll和epoll就是干这个的。将用户socket对应的fd注册进epoll，然后epoll帮你监听哪些socket上有消息到达，这样就避免了大量的无用操作。此时的socket应该采用非阻塞模式。这样，整个过程只在调用select、poll、epoll这些调用的时候才会阻塞，收发客户消息是不会阻塞的，整个进程或者线程就被充分利用起来，这就是事件驱动，所谓的reactor模式。

* 实现原理：Redis 通过IO 多路复用程序 来监听来自客户端的大量连接（或者说是监听多个 socket），它会将感兴趣的事件及类型（读、写）注册到内核中并监听每个事件是否发生。

这样的好处非常明显： I/O 多路复用技术的使用让 Redis 不需要额外创建多余的线程来监听客户端的大量连接，降低了资源的消耗


![redis_I/O多路复用原理](https://github.com/Teahel/JavaLine/blob/main/image/redis_IO%E5%A4%9A%E8%B7%AF%E5%A4%8D%E7%94%A8%E5%8E%9F%E7%90%86%E5%9B%BE.jpg)

#### 使用单线程原因
* 使用单线程模型能带来更好的可维护性，方便开发和调试；
* 使用单线程模型也能并发的处理客户端的请求；
* Redis 服务中运行的绝大多数操作的性能瓶颈都不是 CPU；

#### Redis 给缓存数据设置过期时间
* 过期时间有助于缓解内存的消耗
* 部分业务场景就是需要某个数据只在某一时间段内存在，比如我们的短信验证码可能只在 1 分钟内有效，用户登录的 token 可能只在 1 天内有效。

#### Redis 是如何判断数据是否过期

Redis 通过一个叫做过期字典（可以看作是 hash 表）来保存数据过期的时间。过期字典的键指向 Redis 数据库中的某个 key(键)，过期字典的值是一个 long long 类型的整数，这个整数保存了 key 所指向的数据库键的过期时间（毫秒精度的 UNIX 时间戳）。

* Redis Expire Key基础

redis数据库在数据库服务器中使用了redisDb数据结构体存储过期时间
```
typedef struct redisDb {
dict *dict; /* 键空间 key space */ 
dict *expires; /* 过期字典 */ 
dict *blocking_keys;  /* Keys with clients waiting for data (BLPOP) */ 
dict *ready_keys;   /* Blocked keys that received a PUSH */ 
dict *watched_keys;   /* WATCHED keys for MULTI/EXEC CAS */	
struct evictionPoolEntry *eviction_pool; /* Eviction pool of keys */	
int id; /* Database ID */	
long long avg_ttl;   /* Average TTL, just for stats */	
} redisDb;
```
* 键空间(key space)：dict字典用来保存数据库中的所有键值对
* 过期字典(expires):保存数据库中所有键的过期时间，过期时间用UNIX时间戳表示，且值为long long整数

* 具体原理结构图如下


![redisDb](https://github.com/Teahel/JavaLine/blob/main/image/RedisDb.png)

#### 过期的数据的删除策略

* 惰性删除 ：只会在取出 key 的时候才对数据进行过期检查。不频繁调动cpu，但是可能会造成太多过期 key 没有被删除。

* 定期删除 ： 每隔一段时间抽取一批 key 执行删除过期 key 操作。Redis 底层会限制删除操作执行的时长和频率来减少过期删除操作对 CPU 时间的影响。

* 定期删除对内存更加友好，惰性删除对 CPU 更加友好。Redis 采用的是 定期删除+惰性/懒汉式删除 。

#### Redis 内存淘汰机制（内存不足即将耗尽时触发）

1. volatile-lru（least recently used）：从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰
2. volatile-ttl：从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰
3. volatile-random：从已设置过期时间的数据集（server.db[i].expires）中随机选择数据淘汰
4. allkeys-lru（least recently used）：当内存不足以容纳新写入数据时，在键空间中（全体key集合），移除最近最少使用的 key
5. allkeys-random：从数据集（server.db[i].dict）中任意选择数据淘汰
6. no-eviction：禁止驱逐数据，也就是说当内存不足以容纳新写入数据时，新写入操作会报错。
7. volatile-lfu（least frequently used）：从已设置过期时间的数据集（server.db[i].expires）中挑选最不经常使用的数据淘汰
8. allkeys-lfu（least frequently used）：当内存不足以容纳新写入数据时，在键空间中，移除最不经常使用的 key

#### Redis 持久化机制

两种方式：Redis 的一种持久化方式叫快照（snapshotting，RDB），另一种方式是只追加文件（append-only file, AOF）。

#### 快照（snapshotting）持久化（RDB）

Redis 可以通过创建快照来获得存储在内存里面的数据在某个时间点上的副本。Redis 创建快照之后，可以对快照进行备份，可以将快照复制到其他服务器从而创建具有相同数据的服务器副本（Redis 主从结构，主要用来提高 Redis 性能），还可以将快照留在原地以便重启服务器的时候使用。

快照持久化是 Redis 默认采用的持久化方式，在 Redis.conf 配置文件中默认有此下配置：

```
save 900 1           #在900秒(15分钟)之后，如果至少有1个key发生变化，Redis就会自动触发BGSAVE命令创建快照。

save 300 10          #在300秒(5分钟)之后，如果至少有10个key发生变化，Redis就会自动触发BGSAVE命令创建快照。

save 60 10000        #在60秒(1分钟)之后，如果至少有10000个key发生变化，Redis就会自动触发BGSAVE命令创建快照。

```

#### AOF（append-only file）持久化

与快照持久化相比，AOF 持久化的实时性更好，因此已成为主流的持久化方案。默认情况下 Redis 没有开启 AOF（append only file）方式的持久化，可以通过 appendonly 参数开启：

```
appendonly yes
```
开启 AOF 持久化后每执行一条会更改 Redis 中的数据的命令，Redis 就会将该命令写入到内存缓存 **server.aof_buf** 中，然后再根据 **appendfsync** 配置来决定何时将其同步到硬盘中的 AOF 文件。

AOF 文件的保存位置和 RDB 文件的位置相同，都是通过 dir 参数设置的，默认的文件名是 appendonly.aof。

在 Redis 的配置文件中存在三种不同的 AOF 持久化方式，它们分别是：

```
appendfsync always    #每次有数据修改发生时都会写入AOF文件,这样会严重降低Redis的速度
appendfsync everysec  #每秒钟同步一次，显示地将多个写命令同步到硬盘
appendfsync no        #让操作系统决定何时进行同步
```
为了兼顾数据和写入性能，用户可以考虑 appendfsync everysec 选项 ，让 Redis 每秒同步一次 AOF 文件，Redis 性能几乎没受到任何影响。而且这样即使出现系统崩溃，用户最多只会丢失一秒之内产生的数据。当硬盘忙于执行写入操作的时候，Redis 还会优雅的放慢自己的速度以便适应硬盘的最大写入速度。


R#### edis 4.0 对于持久化机制的优化

Redis 4.0 开始支持 RDB 和 AOF 的混合持久化（默认关闭，可以通过配置项 aof-use-rdb-preamble 开启）。

如果把混合持久化打开，AOF 重写的时候就直接把 RDB 的内容写到 AOF 文件开头。这样做的好处是可以结合 RDB 和 AOF 的优点, 快速加载同时避免丢失过多的数据。当然缺点也是有的， AOF 里面的 RDB 部分是压缩格式不再是 AOF 格式，可读性较差。

* 补充内容：AOF 重写

AOF 重写可以产生一个新的 AOF 文件，这个新的 AOF 文件和原有的 AOF 文件所保存的数据库状态一样，但体积更小。

AOF 重写是一个有歧义的名字，该功能是通过读取数据库中的键值对来实现的，程序无须对现有 AOF 文件进行任何读入、分析或者写入操作。

在执行 BGREWRITEAOF 命令时，Redis 服务器会维护一个 AOF 重写缓冲区，该缓冲区会在子进程创建新 AOF 文件期间，记录服务器执行的所有写命令。当子进程完成创建新 AOF 文件的工作之后，服务器会将重写缓冲区中的所有内容追加到新 AOF 文件的末尾，使得新旧两个 AOF 文件所保存的数据库状态一致。最后，服务器用新的 AOF 文件替换旧的 AOF 文件，以此来完成 AOF 文件重写操作。

### Redis 事务

Redis 可以通过 **MULTI**，**EXEC**，**DISCARD** 和 **WATCH** 等命令来实现事务(transaction)功能。

```
> MULTI
OK
> SET USER "wangcha"
QUEUED
> GET USER
QUEUED
> EXEC
1) OK
2) "wangcha"

```

使用 MULTI 命令后可以输入多个命令。Redis 不会立即执行这些命令，而是将它们放到队列，当调用了 EXEC 命令将执行所有命令。

* 开始事务（MULTI）。
* 命令入队(批量操作 Redis 的命令，先进先出（FIFO）的顺序执行)。
* 执行事务(EXEC)。

你也可以通过 **DISCARD** 命令取消一个事务，它会清空事务队列中保存的所有命令。

```
> MULTI
OK
> SET USER "wangcha"
QUEUED
> GET USER
QUEUED
> DISCARD
OK
```

**WATCH** 命令用于监听指定的键，当调用 **EXEC** 命令执行事务时，如果一个被 **WATCH** 命令监视的键被修改的话，整个事务都不会执行，直接返回失败。

```
> WATCH USER
OK
> MULTI
> SET USER "Guide哥"
OK
> GET USER
Guide哥
> EXEC
ERR EXEC without MULTI
```

### Redis 是不支持 roll back 
官方回复


#### 缓存穿透

大量请求的key在缓存找不到结果，然后又去数据库中查询，导致数据库压力剧增。

处理办法：

* 基本操作，接口层增加校验，如用户鉴权校验，id做基础校验，id<=0的直接拦截；

* 缓存无效 key(方式一)

在redis和数据库都查不到数据的情况下，就写一个key到redis中，并设置一个较短的时间。例如1分钟，30秒。这种方式下如果黑客恶意请求会不断产生无效的key，时间较短可以较少的避免内存占用过久问题。
示例代码如下：

```
public Object getObjectInclNullById(Integer id) {
    // 从缓存中获取数据
    Object cacheValue = cache.get(id);
    // 缓存为空
    if (cacheValue == null) {
        // 从数据库中获取
        Object storageValue = storage.get(key);
        // 缓存空对象
        cache.set(key, storageValue);
        // 如果存储数据为空，需要设置一个过期时间(300秒)
        if (storageValue == null) {
            // 必须设置过期时间，否则有被攻击的风险
            cache.expire(key, 60 );
        }
        return storageValue;
    }
    return cacheValue;
}

```

* 布隆过滤器(方式二)

加入过滤器：对元素进行哈希函数运算，得到的哈希值。将数组中该哈希值为下标位置赋值为1，使用bit数组

判断元素是否存在：对于该元素进行哈希函数运算，然后找到对应下标位置的数值是否为1，为1则证明存在

然后，一定会出现这样一种情况：不同的字符串可能哈希出来的位置相同。 （可以适当增加位数组大小或者调整我们的哈希函数来降低概率）

 布隆过滤器说某个元素存在，小概率会误判。布隆过滤器说某个元素不在，那么这个元素一定不在。因为不存在的元素哈希值可能与存在的元素哈希值位置相同，虽然概率很小。

#### 缓存击穿

缓存中没有数据，数据库中有数据，由于并发量过大，用户同时访问缓存没有数据从而去数据库访问，造成数据库突然压力增大。

处理办法：
* 设置热点数据永远不过期。

* 加互斥锁，互斥锁参考代码如下：

```
public static String getValue(String key) throws InterruptedException {
        // 缓存读数据
        String value = getFromRedis(key);
        if (value == null) {
            // 获取锁
            if (reenLock.tryLock()) {
                try {
                    //从数据库获取数据
                    value = getFromMysql(key);
                    if (value != null) {
                        //更新缓存
                        setDataToCache(key,value);
                    }
                } finally {
                    //解锁
                    reenLock.unLock();
                }

            } else {
                //拿不到锁，暂停100ms再去获得锁
                Thread.sleep(100);
                value = getValue(key);
            }
        }
        return value;
    }
```
### 缓存雪崩

缓存雪崩现象：某一个时间大量的缓存数据失效，例如时间过期，大量数据请求直接访问到了数据库，导致的服务器宕机。

* 处理办法

  * 缓存数据的过期时间设置随机，防止同一时间大量数据过期现象发生。
  * 如果缓存数据库是分布式部署，将热点数据均匀分布在不同搞得缓存数据库中。
  * 设置热点数据永远不过期。
  * 限流，避免同时处理大量的请求。

### 如何保证缓存和数据库数据的一致性？

Cache Aside Pattern（旁路缓存模式）: 更新 DB，然后直接删除 cache 。

如果更新数据库成功，而删除缓存这一步失败的情况的话，简单说两个解决方案：

* 缓存失效时间变短（不推荐，治标不治本） ：我们让缓存数据的过期时间变短，这样的话缓存就会从数据库中加载数据。另外，这种解决办法对于先操作缓存后操作数据库的场景不适用。
* 增加 cache 更新重试机制（常用）： 如果 cache 服务当前不可用导致缓存删除失败的话，我们就隔一段时间进行重试，重试次数可以自己定。如果多次重试还是失败的话，我们可以把当前更新失败的 key 存入队列中，等缓存服务可用之后，再将缓存中对应的 key 删除即可。

### 确实如下部分需要补充
https://cloud.tencent.com/developer/article/1546995

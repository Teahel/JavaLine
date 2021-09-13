#### Redis

#### Redis 单线程模型详解

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



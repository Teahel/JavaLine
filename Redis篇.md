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

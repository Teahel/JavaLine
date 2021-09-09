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



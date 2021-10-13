## synchronized
* 草图
 

* 原理

1. 依赖jvm实现
2. 底层监视器锁(monitor)依靠操作系统互斥锁（Mutex Lock）实现

synchronized 关键字最主要的三种使用方式

- 修饰方法（作用于当前对象实例加锁，进入同步代码前要获得当前对象实例的锁）
```
synchronized void method() {
    //业务代码
}
```
- 修饰静态方法（static 静态资源，不管多少个实例，只有一份。）

```
synchronized static void method() {
    //业务代码
}
```
- 指定加锁对象，对给定对象/类加锁。synchronized(this|object) 表示进入同步代码库前要获得给定对象的锁。synchronized(类.class) 表示进入同步代码前要获得 所有该类 的锁

```
synchronized (this) {
    //业务代码
}
```

* 使用synchronized 实现单例（双重校验锁实现对象单例（线程安全））

```
public class Singleton {

    //需要注意 uniqueInstance 采用 volatile 关键字修饰也是很有必要。
    private volatile static Singleton uniqueInstance;

    public static Singleton getUniqueInstance() {
        if(uniqueInstance == null) {
            synchronized (Singleton.class) {
                uniqueInstance = new Singleton();
            }
        }
        return uniqueInstance;
    }

}
```



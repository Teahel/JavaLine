### ReentrantLock用于替代synchronized加锁带来便捷使用

* synchronized 基于底层操作系统使用互斥锁
* ReentrantLock 基于jvm实现

1. 原理比较

 - synchronized

   1. 基于操作系统实现互斥锁，比较重；
   2. 一直等待，没有额外的尝试机制；
 
-  ReentrantLock

   1. 基于jvm实现，轻量；
   2. 允许额外的尝试操作机制；

2. 代码实现

- synchronized,一直等待直到锁释放，并且报错没有异常捕捉

```
public class SynchronizedUseDemo {

    private int count;

    public int getCount(int a) {
        synchronized (this) {
            count +=a;
        }
        return count;
    }
}

```

- ReentrantLock,额外尝试机制（trylock）,可以等待中断(lockInterruptibly实现)，


```

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {
    private int count;

    //获取锁
    private  final Lock lock = new ReentrantLock();

    public int getCount(int a) {
        try {
            lock.lock();
            count +=a;
        } finally {
            lock.unlock();
        }
        return count;
    }
}

```
- ReentrantLock在1秒内获取锁，如果获取不到返回false，这样可以不用无限制等待

 ```
 if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        ...
    } finally {
        lock.unlock();
    }
}
 ```








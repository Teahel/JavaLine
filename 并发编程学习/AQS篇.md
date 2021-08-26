### AQS学习总结

#### 1.AQS原理分析

CLH(Craig,Landin,and Hagersten)队列是一个虚拟的双向队列（虚拟的双向队列即不存在队列实例，仅存在结点之间的关联关系）。AQS 是将每条请求共享资源的线程封装成一个 CLH 锁队列的一个结点（Node）来实现锁的分配。

CLH队列原理图

![Image of Yaktocat](https://github.com/Teahel/JavaLine/blob/main/image/AQS_CLH.jpg)

如图所示，AQS原理是使用一个volatile修饰的整形来做为一个同步状态，使用CLH队列来对等待线程实现排队机制。AQS使用CAS（campare and swap）对于同步状态进行原子操作。

以下是源码中部分代码
```
 /** waitStatus value to indicate thread has cancelled */
 static final int CANCELLED =  1;
 /** waitStatus value to indicate successor's thread needs unparking */
 static final int SIGNAL    = -1;
 /** waitStatus value to indicate thread is waiting on condition */
 static final int CONDITION = -2;
 /**
  * waitStatus value to indicate the next acquireShared should
  * unconditionally propagate
  */
 static final int PROPAGATE = -3;
```
        
```
        
// volatile 修改同步状态
private volatile int state;

// 获取状态
protected final int getState() {return state; }
// 设置状态，这个操作会将赋值写入内存
protected final void setState(int newState) {    state = newState;   }
// 和expect值比较，如果正确就赋值为update
protected final boolean compareAndSetState(int expect, int update) {
        // 原子操作（CAS）
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }
    
```
#### 2.AQS 定义两种资源共享方式

* Exclusive（独占）：只有一个线程能执行，如 ReentrantLock。又可分为公平锁和非公平锁：
公平锁：按照线程在队列中的排队顺序，先到者先拿到锁
非公平锁：当线程要获取锁时，无视队列顺序直接去抢锁，谁抢到就是谁的

* Share（共享）：多个线程可同时执行，如 CountDownLatch、Semaphore、 CyclicBarrier、ReadWriteLock 我们都会在后面讲到。

不同的自定义同步器争用共享资源的方式也不同。自定义同步器在实现时只需要实现共享资源 state 的获取与释放方式即可，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出队等），AQS 已经在顶层实现好了。

#### 3. AQS 模板方法


```
 isHeldExclusively()//该线程是否正在独占资源。只有用到condition才需要去实现它。
tryAcquire(int)//独占方式。尝试获取资源，成功则返回true，失败则返回false。
tryRelease(int)//独占方式。尝试释放资源，成功则返回true，失败则返回false。
tryAcquireShared(int)//共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
tryReleaseShared(int)//共享方式。尝试释放资源，成功则返回true，失败则返回false。
```


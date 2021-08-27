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

 * ReentrantLock
 
   * state 初始化为 0，表示未锁定状态。A 线程 lock()时，会调用 tryAcquire()独占该锁并将 state+1。此后，其他线程再 tryAcquire()时就会失败，直到 A 线程 unlock()到 state=0（即释放锁）为止，其它线程才有机会获取该锁。当然，释放锁之前，A 线程自己是可以重复获取此锁的（state 会累加），这就是可重入的概念。但要注意，获取多少次就要释放多么次，这样才能保证 state 是能回到零态的。

 * CountDownLatch
 
   * 任务分为 N 个子线程去执行，state 也初始化为 N（注意 N 要与线程个数一致）。这 N 个子线程是并行执行的，每个子线程执行完后 countDown() 一次，state 会 CAS(Compare and Swap)减 1。等到所有子线程都执行完后(即 state=0)，会 unpark()主调用线程，然后主调用线程就会从 await() 函数返回，继续后余动作。 

* 一般来说，自定义同步器要么是独占方法，要么是共享方式，他们也只需实现tryAcquire-tryRelease、tryAcquireShared-tryReleaseShared中的一种即可。但 AQS 也支持自定义同步器同时实现独占和共享两种方式，如ReentrantReadWriteLock。


### AQS学习总结

#### 1.AQS原理分析

**CLH(Craig,Landin,and Hagersten)** 队列是一个虚拟的双向队列（虚拟的双向队列即不存在队列实例，仅存在结点之间的关联关系）。AQS 是将每条请求共享资源的线程封装成一个 CLH 锁队列的一个结点（Node）来实现锁的分配。

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
```
```
// 获取状态
protected final int getState() {return state; }
```
```
// 设置状态，这个操作会将赋值写入内存
protected final void setState(int newState) {    state = newState;   }
```
```
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

#### 4.AQS 组件总结

* Semaphore(信号量)-允许多个线程同时访问： 

synchronized 和 ReentrantLock 都是一次只允许一个线程访问某个资源，Semaphore(信号量)可以指定多个线程同时访问某个资源。

* CountDownLatch （倒计时器）： 

CountDownLatch 是一个同步工具类，用来协调多个线程之间的同步。这个工具通常用来控制线程等待，它可以让某一个线程等待直到倒计时结束，再开始执行。

* CyclicBarrier(循环栅栏)： 

CyclicBarrier 和 CountDownLatch 非常类似，它也可以实现线程间的技术等待，但是它的功能比 CountDownLatch 更加复杂和强大。主要应用场景和 CountDownLatch 类似。CyclicBarrier 的字面意思是可循环使用（Cyclic）的屏障（Barrier）。它要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活。CyclicBarrier 默认的构造方法是 CyclicBarrier(int parties)，其参数表示屏障拦截的线程数量，每个线程调用 await() 方法告诉 CyclicBarrier 我已经到达了屏障，然后当前线程被阻塞。


#### 5.CountDownLatch 案例

```
public class CountDownLatchDemo {
    // 处理文件的数量
    private static final int threadCount = 6;

    public static void main(String[] args) throws InterruptedException {
        // 创建一个具有固定线程数量的线程池对象（推荐使用构造方法创建）
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadnum = i;
            threadPool.execute(() -> {
                try {
                    //处理文件的业务操作
                    //......
                   
                } catch (InterruptedException e) {
                   e.printStackTrace();
                } finally {
                    //表示一个文件已经被完成
                    countDownLatch.countDown();
                }

            });
        }
        countDownLatch.await();
        threadPool.shutdown();
        System.out.println("finish");
    }
}
```


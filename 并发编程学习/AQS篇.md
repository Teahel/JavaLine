### AQS学习总结

CLH队列原理图

![Image of Yaktocat](https://github.com/Teahel/JavaLine/blob/main/image/AQS_CLH.jpg)

如图所示，AQS原理是使用一个volatile修饰的整形来做为一个同步状态，使用CLH队列来对等待线程实现排队机制。AQS使用CAS（campare and swap）对于同步状态进行原子操作。

以下是源码中部分代码
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



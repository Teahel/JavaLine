### ThreadLocal 每个线程专属的本地变量，相互之间不影响。

创建了一个ThreadLocal变量，那么访问这个变量的每个线程都会有这个变量的本地副本。
使用 get（） 和 set（） 方法来获取默认值或将其值更改为当前线程所存的副本的值，从而避免了线程安全问题。

使用案例
```
public class ThreadLocalExample  implements Runnable{

    // SimpleDateFormat 不是线程安全的，所以每个线程都要有自己独立的副本
    private static final ThreadLocal<LocalDateTime> showTime = ThreadLocal.withInitial(() -> LocalDateTime.now());

    public static void main(String[] args) throws InterruptedException {
        ThreadLocalExample obj = new ThreadLocalExample();
        for(int i=0 ; i<10; i++){
            Thread t = new Thread(obj, ""+i);
            Thread.sleep(new Random().nextInt(1000));
            t.start();
        }
    }

    @Override
    public void run() {
        System.out.println("Thread Name= "+Thread.currentThread().getName()+" time = "+showTime.get());
        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //这里修改该线程的数据
        showTime.set(LocalDateTime.now());

        System.out.println("Thread Name= "+Thread.currentThread().getName()+" time = "+showTime.get().toString());
    }
}

```


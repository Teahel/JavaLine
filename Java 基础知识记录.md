### String StringBuffer 和 StringBuilder 的区别是什么? String 为什么是不可变的?

String 类中使用 **final** 关键字修饰字符数组来保存字符串，**private final char value[]**，所以**String** 对象是不可变的。

StringBuilder 与 StringBuffer 都继承自 AbstractStringBuilder 类，在 AbstractStringBuilder 中也是使用字符数组保存字符串char[]value 但是没有用 final 关键字修饰，所以这两种对象都是可变的。

StringBuilder 与 StringBuffer 的构造方法都是调用父类构造方法也就是AbstractStringBuilder 实现的。



**AbstractStringBuilder.java**

```
abstract class AbstractStringBuilder implements Appendable, CharSequence {
    /**
     * The value is used for character storage.
     */
    char[] value;

    /**
     * The count is the number of characters used.
     */
    int count;

    AbstractStringBuilder(int capacity) {
        value = new char[capacity];
    }}

```

### 线程安全性
**String** 中的对象是不可变的，也就可以理解为常量，线程安全。**AbstractStringBuilder** 是 **StringBuilder** 与 **StringBuffer** 的公共父类，定义了一些字符串的基本操作，如 **expandCapacity、append、insert、indexOf** 等公共方法。StringBuffer 对方法加了同步锁或者对调用的方法加了同步锁，所以是线程安全的。StringBuilder 并没有对方法进行加同步锁，所以是非线程安全的。

### 性能
每次对 **String** 类型进行改变的时候，都会生成一个新的 **String** 对象，然后将指针指向新的 **String** 对象。**StringBuffer** 每次都会对 **StringBuffer** 对象本身进行操作，而不是生成新的对象并改变对象引用。相同情况下使用 **StringBuilder** 相比使用 **StringBuffer** 仅能获得 **10%~15%** 左右的性能提升，但却要冒多线程不安全的风险。

### Object 类的常见方法总结
Object 类是一个特殊的类，是所有类的父类。它主要提供了以下 11 个方法：
```
//native方法，用于返回当前运行时对象的Class对象，使用了final关键字修饰，故不允许子类重写。
public final native Class<?> getClass()
//native方法，用于返回对象的哈希码，主要使用在哈希表中，比如JDK中的HashMap。
public native int hashCode() 
//用于比较2个对象的内存地址是否相等，String类对该方法进行了重写用户比较字符串的值是否相等。
public boolean equals(Object obj)

protected native Object clone() throws CloneNotSupportedException
//naitive方法，用于创建并返回当前对象的一份拷贝。一般情况下，对于任何对象 x，表达式 x.clone() != x 为true，x.clone().getClass() == x.getClass() 为true。
//Object本身没有实现Cloneable接口，所以不重写clone方法并且进行调用的话会发生CloneNotSupportedException异常。

public String toString()//返回类的名字@实例的哈希码的16进制的字符串。建议Object所有的子类都重写这个方法。

public final native void notify()//native方法，并且不能重写。唤醒一个在此对象监视器上等待的线程(监视器相当于就是锁的概念)。如果有多个线程在等待只会任意唤醒一个。

public final native void notifyAll()//native方法，并且不能重写。跟notify一样，唯一的区别就是会唤醒在此对象监视器上等待的所有线程，而不是一个线程。

//native方法，并且不能重写。暂停线程的执行。注意：sleep方法没有释放锁，而wait方法释放了锁 。timeout是等待时间。
public final native void wait(long timeout) throws InterruptedException

//多了nanos参数，这个参数表示额外时间（以毫微秒为单位，范围是 0-999999）。 所以超时的时间还需要加上nanos毫秒。
public final void wait(long timeout, int nanos) throws InterruptedException

public final void wait() throws InterruptedException//跟之前的2个wait方法一样，只不过该方法一直等待，没有超时时间这个概念

protected void finalize() throws Throwable { }//实例被垃圾回收器回收的时候触发的操作

```

### 异常分类






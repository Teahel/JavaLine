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


![throwable](https://github.com/Teahel/JavaLine/blob/main/image/throwable.jpg)

#### Throwable 类常用的方法

* public String getMessage() //返回异常的简单描述
* public String getLocalizedMessage()//返回异常对象的本地化描述，子类会覆盖这个方法，为了创建一个本地化信息，如果子类没有覆盖这个方法，那么默认返回和**getMessage**一样
* public String toString() //返回异常发生时的详细信息
* public void printStackTrace()//在控制台上打印 Throwable 对象封装的异常信息

#### try-catch-finally 异常捕捉处理

**try**：用于捕捉异常，
**catch**： 处理异常，可以存在多个，分类处理
**finally** ：无论是否有异常都会执行，就算是在try部分，或者 catch 部分return 执行之前也会执行。


### BigDecimal
商业计算使用，金额，或者更重要计算

```
// 第一种用new一个对象的方式
BigDecimal b1 = new BigDecimal("0.0");
// 第二种用内部方法获取
BigDecimal b2 = BigDecimal.valueOf(0.0);
```

* 加减乘除使用方式

Subtract(减法)

```
        BigDecimal bigdecimal = new BigDecimal("0");
        bigdecimal.subtract()
```

add(加法)

```
        BigDecimal bigdecimal = new BigDecimal("0");
        bigdecimal.add()
```

除法
```
BigDecimal decimal = new BigDecimal
 decimal.divide(2,2,BigDecimal.ROUND_HALF_UP)
```
multiply(乘法)
```
BigDecimal decimal = new BigDecimal
 decimal.multiply(2,2,BigDecimal.ROUND_HALF_UP)
```
#### roundingMode
```
public static void main(String[] args) 
{
	//ROUND_DOWN————删除多余的小数
	BigDecimal bd1 = new BigDecimal("2.0999");
	System.out.println(bd1.setScale(2, BigDecimal.ROUND_DOWN));
	
	/*
	ROUND_UP——进位处理
	详解：假设2.18000001保留2位小数做ROUND_UP处理，把8后面的数拿出来看做一个整数，
	如果这个整数大于0，则做进位处理，否则不进位，其结果是还2.18。8后面无数字结果还是2.18
	*/
	BigDecimal bd2 = new BigDecimal("2.180000000");
	System.out.println(bd2.setScale(2, BigDecimal.ROUND_UP));
	
	//ROUND_HALF_UP——四舍五入，和数学上的四舍五入概念一样
	BigDecimal bd3 = new BigDecimal("2.1865186");
	System.out.println(bd3.setScale(3, BigDecimal.ROUND_UP));
	
	/*
	ROUND_HALF_DOWN——1-5不进位，6或以上才进位
	详解：假设2.185000001保留2位小数做ROUND_HALF_DOWN处理，且8后一位是5，把5后面的数拿出来看做一个整数，
	如果这个整数大于0，则按照ROUND_HALF_UP规则处理，其结果为2.19。如果这个整数为0则，其结果为2.18
	*/
	BigDecimal bd4 = new BigDecimal("2.18500000");
	System.out.println(bd4.setScale(2, BigDecimal.ROUND_HALF_DOWN));
	BigDecimal bd5 = new BigDecimal("2.18500001");
	System.out.println(bd5.setScale(2, BigDecimal.ROUND_HALF_DOWN));
	
	/*
	ROUND_HALF_EVEN:
	如果第2位是奇数,则做ROUND_HALF_UP
	如果第2位是偶数,则做ROUND_HALF_DOWN
	*/
    BigDecimal bd6 = new BigDecimal("31.1150");
    System.out.println(bd6.setScale(2, BigDecimal.ROUND_HALF_EVEN));
    BigDecimal bd7 = new BigDecimal("31.1250001");
    System.out.println(bd7.setScale(2, BigDecimal.ROUND_HALF_EVEN));
}

```

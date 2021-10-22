### Java 注解编写

注解在很多地方都是用到，极大方便代码耦合，也十分编辑。

### 元注解

有一些注解可以修饰其他注解，这些注解就称为元注解（meta annotation）。Java标准库已经定义了一些元注解，我们只需要使用元注解，通常不需要自己去编写元注解。
**@Target**

**@Target** 应用于源码的哪些位置：

* 类或接口：ElementType.TYPE；
* 字段：ElementType.FIELD；
* 方法：ElementType.METHOD；
* 构造方法：ElementType.CONSTRUCTOR；
* 方法参数：ElementType.PARAMETER。

### 作用在方法上案例

```
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserLog {

}
```
作用在方法或者字段上使用 **@Target({ElementType.METHOD,ElementType.FIELD})**
```
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserLog {
}
```
**@Retention**
* @Retention定义了Annotation的生命周期：
- 仅编译期：RetentionPolicy.SOURCE；
- 仅class文件：RetentionPolicy.CLASS；
- 运行期：RetentionPolicy.RUNTIME。

**@Repeatable** 定义注解是否可以重复;具体代码在**annotation**项目
```
@Target(ElementType.TYPE)
@Repeatable(Infomations.class)
public @interface Infomation {

    String place() default "shenzhen";

    String house() default "14level";
}

@Target(ElementType.TYPE)
public @interface Infomations {

    Infomation[] value();
}



```
经过**Repeatable** 修饰之后，可以多次添加**@Infomation**

```
@Infomation(place = "1",house = "2")
@Infomation(place = "3",house = "4")
public class Hello {
}
```
**@Inherited** 
使用@Inherited定义子类是否可继承父类定义的Annotation。
**@Inherited** 仅针对**@Target(ElementType.TYPE)** 类型的annotation有效，并且仅针对class的继承，对interface的继承无效：

```
@Inherited
@Target(ElementType.TYPE)
public @interface User {
    String username() default "heihei";
}

```

```
@User(username = "heiehei")
public class Person {
}

```
```
public class Student extends Person{
}
```



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


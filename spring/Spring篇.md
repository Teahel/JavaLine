# Spring

## Spring IOC & AOP 


### Spring IOC 







### Spring中的Bean是线程安全的吗？

* Spring容器中的Bean是否线程安全，容器本身并没有提供Bean的线程安全策略，因此可以说Spring容器中的Bean本身不具备线程安全的特性，但是具体还是要结合具体scope的Bean去研究。+

* 1、 Spring 的 bean 作用域（scope）类型

   * singleton:单例，默认作用域。

   * prototype:原型，每次请求创建一个新对象。

   * request:每一次 HTTP 请求都会产生一个新的 bean，该 bean 仅在当前 HTTP request 内有效。适用于WebApplicationContext环境下。
  （WebApplicationContext,是继承于ApplicationContext的一个接口，扩展了ApplicationContext，是专门为Web应用准备的，它允许从相对于Web根目录的路径中装载配置文件完成初始化。）

   * session:会话，同一个会话共享一个实例，不同会话使用不用的实例（不同会话会新建）。

   * global-session:全局会话，所有会话共享一个实例。

*  2、单例Bean

对于单例Bean,所有线程都共享一个单例实例Bean,因此是存在资源的竞争。

如果单例Bean,是一个无状态Bean，也就是线程中的操作不会对Bean的成员执行查询以外的操作，那么这个单例Bean是线程安全的。比如Spring mvc 的 Controller、Service、Dao等，这些Bean大多是无状态的，只关注于方法本身。

对于有状态的bean，Spring官方提供的bean，一般提供了通过ThreadLocal去解决线程安全的方法，比如RequestContextHolder、TransactionSynchronizationManager、LocaleContextHolder等。

* 3、原型Bean

每次创建一个新对象，也就是线程之间并不存在Bean共享，自然是不会有线程安全的问题。


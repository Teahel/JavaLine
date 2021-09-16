## Spring IOC

### 什么是 IoC
IoC （Inversion of control ）控制反转/反转控制。它是一种思想不是一个技术实现。描述的是：Java 开发领域对象的创建以及管理的问题。

实际：类和类之间的依赖关系通过ioc容器管理，并有ioc容器完成依赖注入，Spring 容器负责实例化、配置和组装称为 bean 的对象，以及管理它们的生命周期。通过配置或者注解方式就可以实现对象创建。
从而实现，降低代码耦合性降低，对象由容器统一管理和资源管理更加便捷；

Spring 容器负责实例化、配置和组装称为 bean 的对象，以及管理它们的生命周期。

这里有篇文章解释什么是DI和IOC! 

[IOC&DI](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)

### IOC底层实现原理

#### BeanFactory 体系
![BeanFactory 体系](https://github.com/Teahel/JavaLine/blob/main/image/beanfactory.png)


### 手动实例化

```
ApplicationContext context
  = new ClassPathXmlApplicationContext("applicationContext.xml");
```

### @Configuration 配置方式

```
@Configuration
public class AppConfig {

    @Bean
    public Item item1() {
        return new ItemImpl1();
    }

    @Bean
    public Store store() {
        return new Store(item1());
    }
}
```
### setter-based DI

```
@Bean
public Store store() {
    Store store = new Store();
    store.setItem(item1());
    return store;
}
```

### Field-Based
```
public class Store {
    @Autowired
    private Item item; 
}
```


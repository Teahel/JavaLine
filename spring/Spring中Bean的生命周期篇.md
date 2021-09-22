
### Spring Bean生命周期篇

### ApplicationContext Vs BeanFactory区别（两个IOC Containers 不同）

* Lazy Loading vs. Eager Loading
  * BeanFactory loads beans on-demand, while ApplicationContext loads all beans at startup. Thus, BeanFactory is lightweight as compared to ApplicationContext.
  * ApplicationContext is considered a heavy IOC container because its eager-loading strategy loads all the beans at startup. BeanFactory is lightweight by comparison and could be handy in memory-constrained systems. Nevertheless, we'll see in the next sections why ApplicationContext is preferred for most use cases.
*  Enterprise Application Features
   * ApplicationContext enhances BeanFactory in a more framework-oriented style and provides several features that are suitable for enterprise applications.
For instance, it provides messaging (i18n or internationalization) functionality, event publication functionality, annotation-based dependency injection, and easy integration with Spring AOP features.Apart from this, the ApplicationContext supports almost all types of bean scopes, but the BeanFactory only supports two scopes — Singleton and Prototype. Therefore, it's always preferable to use ApplicationContext when building complex enterprise applications.

* Automatic Registration of BeanFactoryPostProcessor and BeanPostProcessor
   * The ApplicationContext automatically registers BeanFactoryPostProcessor and BeanPostProcessor at startup. On the other hand, the BeanFactory does not register these interfaces automatically.

   * it's always advisable to use ApplicationContext because Spring 2.0 (and above) heavily uses BeanPostProcessor.
It's also worth noting that if you're using the plain BeanFactory, then features like transactions and AOP will not take effect (at least not without writing extra lines of code). This may lead to confusion because nothing will look wrong with the configuration. 
  
上述总结来自[ApplicationContext和BeanFactory的区别](https://www.baeldung.com/spring-beanfactory-vs-applicationcontext)
总结比较到位的，以及有案例解释。


### Spring 中Bean的生命周期（需要更深入理解）

* Bean factory implementations should support the standard bean lifecycle interfaces as far as possible. The full set of initialization methods and their standard order is:
  * 1、BeanNameAware's setBeanName
  * 2、BeanClassLoaderAware's setBeanClassLoader
  * 3、BeanFactoryAware's setBeanFactory
  * 4、EnvironmentAware's setEnvironment
  * 5、EmbeddedValueResolverAware's setEmbeddedValueResolver
  * 6、ResourceLoaderAware's setResourceLoader (only applicable when running in an application context)
  * 7、ApplicationEventPublisherAware's setApplicationEventPublisher (only applicable when running in an application context)
  * 8、MessageSourceAware's setMessageSource (only applicable when running in an application context)
  * 9、ApplicationContextAware's setApplicationContext (only applicable when running in an application context)
  * 10、ServletContextAware's setServletContext (only applicable when running in a web application context)
  * 11、postProcessBeforeInitialization methods of BeanPostProcessors
  * 12、InitializingBean's afterPropertiesSet
  * 13、a custom init-method definition
  * 14、postProcessAfterInitialization methods of BeanPostProcessors

* On shutdown of a bean factory, the following lifecycle methods apply:
  * 15、postProcessBeforeDestruction methods of DestructionAwareBeanPostProcessors
  * 16、DisposableBean's destroy
  * 17、a custom destroy-method definition
  
### 代码案列解析

**（一）创建实例**
```
@Configuration
public class MainConfigOfLifeCycle {


    @Bean(initMethod = "initMethod",destroyMethod = "destroyMethod",name = "user")
    public User user (){
        System.out.println("创建实例.....");
        return  new User();
    }
}
```


**（二）User**
```
//尽量原始一些，不使用太多便捷注解
@Configuration
public class User implements BeanNameAware, BeanClassLoaderAware, BeanFactoryAware,
        EnvironmentAware, EmbeddedValueResolverAware, ResourceLoaderAware, ApplicationEventPublisherAware, MessageSourceAware,
        ApplicationContextAware, ServletContextAware, InitializingBean , DisposableBean{

    // 初始化方法
    public void initMethod(){
        System.out.println("initMethod ....");
    }
    // 销毁方法
    public void destroyMethod() {
        System.out.println("destroyMethod ....");
    }

   /* @PostConstruct： PostConstruct注解作用在方法上，在依赖注入完成后进行一些初始化操作。这个方法在类被放入service之前被调用，所有支持依赖项注入的类都必须支持此注解。
    * @PreDestroy：在容器销毁bean之前通知我们进行清理工作
    */

    @PostConstruct
    public void myPostConstruct() {
        System.out.println("PostConstruct注解作用在方法上，在依赖注入完成后进行一些初始化操作。 ....");
    }

    // @PreDestroy：在容器销毁bean之前通知我们进行清理工作
    @PreDestroy
    public void myDestroy() {
        System.out.println("@PreDestroy：在容器销毁bean之前通知我们进行清理工作 ....");
    }

    // 将 bean 类加载器提供给 bean 实例的回调。(在普通属性设置之后调用，在InitializinngBean.afterPropertiesSet()方法之前调用)
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("classLoader.....");
    }

    // 传入Bean工厂的实例 (在普通属性设置之后调用，在InitializinngBean.afterPropertiesSet()方法之前调用)
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("beanFactory.....");
    }

    // 在创建此 bean 的 bean 工厂中设置 bean 的名称。(在普通属性设置之后调用，在InitializinngBean.afterPropertiesSet()方法之前调用)
    @Override
    public void setBeanName(String name) {
        System.out.println("beanName.....");

    }

    //在设置所有 bean 属性之后由包含 BeanFactory 调用
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet.....");

    }

    //设置此对象运行所在的 ApplicationContext。通常此调用将用于初始化对象。(在普通属性设置之后调用，在InitializinngBean.afterPropertiesSet()方法之前调用)
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("applicationContext.....");

    }

    //设置运行此对象的 ApplicationEventPublisher。
    //在填充普通 bean 属性之后但在初始化回调（如 InitializingBean 的 afterPropertiesSet 或自定义初始化方法）之前调用。
    // 在 ApplicationContextAware 的 setApplicationContext 之前调用。
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        System.out.println("applicationEventPublisher.....");

    }

    //设置 StringValueResolver 以用于解析嵌入的定义值。
    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        System.out.println("resolver.....");
    }

    //设置该实例运行的环境。(我们运行时有开发环境dev,生产环境 pro,测试环境 test，如：application-test.yml为测试环境专有的配置文件)
    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("environment.....");
    }

    // 设置此对象运行所在的 MessageSource（消息源）。
    //在填充普通 bean 属性之后但在初始化回调（如 InitializingBean 的 afterPropertiesSet 或自定义初始化方法）之前调用。
    // 在 ApplicationContextAware 的 setApplicationContext 之前调用。
    @Override
    public void setMessageSource(MessageSource messageSource) {
        System.out.println("messageSource.....");

    }

    public void  getUsername() {
        System.out.println("litianjun!");
    }

    // 设置运行此对象的资源加载器。
    //在填充普通 bean 属性之后但在初始化回调（如 InitializingBean 的 afterPropertiesSet 或自定义初始化方法）之前调用。
    // 在 ApplicationContextAware 的 setApplicationContext 之前调用。
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        System.out.println("resourceLoader.....");
    }

    // 在web application情况下赋值
    //在填充普通 bean 属性之后但在初始化回调（如 InitializingBean 的 afterPropertiesSet 或自定义初始化方法）之前调用。
    // 在 ApplicationContextAware 的 setApplicationContext 之后调用。
    @Override
    public void setServletContext(ServletContext servletContext) {
        System.out.println("servletContext.....");
    }


    //由包含此bean的 BeanFactory 调用去销毁该bean。
    @Override
    public void destroy() throws Exception {
        System.out.println("由包含此bean的 BeanFactory 调用去销毁该bean.......");
    }

 }
```


在初始化方法前后执行的方法，以及预销毁之前执行的方法。
注入容器中，其他bean会自动调用，其中DestructionAwareBeanPostProcessor继承BeanPostProcessor。
```
@Component
public class MyBeanPostProcessor implements DestructionAwareBeanPostProcessor  {

    //在初始化之前工作
    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessBeforeInitialization:"+bean+" -"+beanName);
        }
        return bean;
    }

    //在初始化之后工作
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessAfterInitialization:"+bean+" -"+beanName);
        }
        return bean;
    }

    //在销毁之前将此BeanPostProcessor 应用于给定的bean实例。能够调用自定义回调
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessBeforeDestruction:"+bean+" -"+beanName);
        }
    }
}
```

**使用ApplicationContext容器**
使用注解方式，
```
public class SpringBeanlifeCycleTest{

    @Test
    public void Test(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.example.springbeanlifecycle");
        User user = context.getBean(User.class);
        user.getUsername();
        //容器关闭，Bean注销
        context.close();
    }
}
```

执行顺序如下：
```
创建实例.....
beanName.....
classLoader.....
beanFactory.....
environment.....
resolver.....
resourceLoader.....
applicationEventPublisher.....
messageSource.....
applicationContext.....
postProcessBeforeInitialization: com.example.springbeanlifecycle.User@7161d8d1 -user
PostConstruct注解作用在方法上，在依赖注入完成后进行一些初始化操作。 ....
afterPropertiesSet.....
initMethod ....
postProcessAfterInitialization: com.example.springbeanlifecycle.User@7161d8d1 -user
litianjun!
23:47:00.343 [main] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@4d826d77, started on Wed Sep 22 23:47:00 CST 2021
postProcessBeforeDestruction: com.example.springbeanlifecycle.User@7161d8d1 -user
@PreDestroy：在容器销毁bean之前通知我们进行清理工作 ....
由包含此bean的 BeanFactory 调用去销毁该bean.......
destroyMethod ....
```




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

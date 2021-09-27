package com.example.springbeanlifecycle;

import javafx.beans.binding.ObjectBinding;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

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
    // 使用事件发布者发布消息: 可以通过 ApplicationEventPublisher 的 publishEvent() 方法发布消息。

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

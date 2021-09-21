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
        ApplicationContextAware, ServletContextAware, InitializingBean , DestructionAwareBeanPostProcessor, DisposableBean{

    public void initMethod(){
        System.out.println("initMethod ....");
    }

    public void destroyMethod() {
        System.out.println("destroyMethod ....");
    }

   /* @PostConstruct： PostConstruct注解作用在方法上，在依赖注入完成后进行一些初始化操作。这个方法在类被放入service之前被调用，所有支持依赖项注入的类都必须支持此注解。
    * @PreDestroy：在容器销毁bean之前通知我们进行清理工作
    */

    @PostConstruct
    public void myPostConstruct() {
        System.out.println("PostConstruct ....");
    }

    // @PreDestroy：在容器销毁bean之前通知我们进行清理工作
    @PreDestroy
    public void myDestroy() {
        System.out.println("myDestroy ....");
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("classLoader.....");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("beanFactory.....");

    }

    @Override
    public void setBeanName(String name) {
        System.out.println("beanName.....");

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet.....");

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("applicationContext.....");

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        System.out.println("applicationEventPublisher.....");

    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        System.out.println("resolver.....");

    }

    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("environment.....");

    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        System.out.println("messageSource.....");

    }

    public void  getUsername() {
        System.out.println("litianjun!");
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        System.out.println("resourceLoader.....");
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        System.out.println("servletContext.....");

    }


    //在销毁 bean 时由包含 BeanFactory 调用。
    @Override
    public void destroy() throws Exception {
        System.out.println("destroy.......");
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        System.out.println("postProcessBeforeDestruction.....");
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return DestructionAwareBeanPostProcessor.super.requiresDestruction(bean);
    }

 /*   @Bean
    public MyBeanPostProcessor getBeanPostProcessor(){
        return new MyBeanPostProcessor();
    }*/
}

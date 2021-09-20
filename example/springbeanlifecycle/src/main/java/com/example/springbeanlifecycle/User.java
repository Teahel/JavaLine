package com.example.springbeanlifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

//尽量原始一些，不使用太多便捷注解

public class User implements BeanNameAware, BeanClassLoaderAware, BeanFactoryAware,
        EnvironmentAware, EmbeddedValueResolverAware, ResourceLoaderAware, ApplicationEventPublisherAware, MessageSourceAware,
        ApplicationContextAware, ServletContextAware, BeanPostProcessor, InitializingBean {

    public User (){
        System.out.println("创建实例.....");
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

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        System.out.println("resourceLoader.....");
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        System.out.println("servletContext.....");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(bean+"|"+beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(bean+"|"+beanName);
        return bean;
    }


    public void  getUsername() {
        System.out.println("litianjun!");
    }

}

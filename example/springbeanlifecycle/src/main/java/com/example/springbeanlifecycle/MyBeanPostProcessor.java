package com.example.springbeanlifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    //在初始化之前工作
    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessBeforeInitialization:"+bean+"|"+beanName);
        }
        return bean;
    }

    //在初始化之后工作
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessAfterInitialization:"+bean+"|"+beanName);
        }
        return bean;
    }
}

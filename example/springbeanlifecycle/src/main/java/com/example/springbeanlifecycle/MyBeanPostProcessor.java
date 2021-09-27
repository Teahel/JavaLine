package com.example.springbeanlifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class MyBeanPostProcessor implements DestructionAwareBeanPostProcessor  {

    //在初始化之前工作
    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessBeforeInitialization: "+bean+" -"+beanName);
        }
        return bean;
    }

    //在初始化之后工作
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessAfterInitialization: "+bean+" -"+beanName);
        }
        return bean;
    }

    //在销毁之前将此BeanPostProcessor 应用于给定的bean实例。能够调用自定义回调
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof User) {
            System.out.println("postProcessBeforeDestruction: "+bean+" -"+beanName);
        }
    }
}

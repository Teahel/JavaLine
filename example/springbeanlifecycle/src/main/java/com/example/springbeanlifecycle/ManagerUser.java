package com.example.springbeanlifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

public class ManagerUser extends User implements DestructionAwareBeanPostProcessor, DisposableBean {

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
}

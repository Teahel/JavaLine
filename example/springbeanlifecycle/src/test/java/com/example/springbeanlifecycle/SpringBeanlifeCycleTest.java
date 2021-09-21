package com.example.springbeanlifecycle;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringBeanlifeCycleTest{

    @Test
    public void Test(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.example.springbeanlifecycle");
        User user = context.getBean(User.class);
        user.getUsername();
        context.close();
    }
}

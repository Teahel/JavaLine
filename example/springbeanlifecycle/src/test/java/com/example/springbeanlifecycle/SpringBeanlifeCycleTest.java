package com.example.springbeanlifecycle;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringBeanlifeCycleTest{

    @Test
    public void Test(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(User.class);
        User user = context.getBean(User.class);
        user.getUsername();
    }
}

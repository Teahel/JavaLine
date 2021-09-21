package com.example.springbeanlifecycle;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MainConfigOfLifeCycle {


    @Bean(initMethod = "initMethod",destroyMethod = "destroyMethod",name = "user")
    public User user (){
        System.out.println("创建实例.....");
        return  new User();
    }
}

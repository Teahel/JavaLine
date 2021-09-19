package com.teahel.aop;

import org.springframework.stereotype.Component;

@Component
public class User {
    //预增强的方法
    public void addUser(){
        System.out.println("目标方法：添加账户！");
    }
}

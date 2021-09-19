package com.teahel.aop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class AopTest extends AopApplicationTests {

    @Autowired
    private User user;

    @Test
    public void aopTest(){
        System.out.println("测试开始。。。。。。");
        user.addUser();
        System.out.println("测试结束。。。。。。");
    }
}

package com.example.annotation;

import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-10-21
 */
@Aspect
@Component
public class UserAspect {

    @Pointcut("@annotation(com.example.annotation.UserLog)")
    public void find() {

    }

    @Before("find()")
    public void before (){
        System.out.println("Before测试");
    }

    @Around("find()")
    public void after() {
        System.out.println("after后来");
    }
}

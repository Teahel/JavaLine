package com.teahel.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class UserProxy {

    //针对该类下所有的方法:execution(* com.teahel.aop.User.*(..))
    //针对该类下指定方法：execution(* com.teahel.aop.User.add(..))
    @Pointcut("execution(* com.teahel.aop.User.*(..))")
    public void pointCut() {

    }

    @Before("pointCut()")
    public void before(){
        System.out.println("before......");
    }

    @After("pointCut()")
    public void after(){
        System.out.println("after......");
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("around前......");
        Object o = pjp.proceed();
        System.out.println("around后......");
        return o;
    }

    @AfterReturning("pointCut()")
    public void afterReturning(){
        System.out.println("afterReturning......");
    }

    @AfterThrowing("pointCut()")
    public void afterThrowing(){
        System.out.println("afterThrowing......");
    }

}

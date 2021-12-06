package com.ioc.test.demo;

import com.ioc.test.annotation.IocAutowired;
import com.ioc.test.annotation.IocComponent;

@IocComponent
public class People {


    public void test(){
        System.out.println("test 方法执行！");
    }

}

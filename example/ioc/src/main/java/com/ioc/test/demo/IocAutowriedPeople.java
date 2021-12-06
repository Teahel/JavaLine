package com.ioc.test.demo;

import com.ioc.test.annotation.IocAutowired;
import com.ioc.test.annotation.IocComponent;

@IocComponent
public class IocAutowriedPeople {

    @IocAutowired
    private People peoples;

    public void show() {
        System.out.println("show 方法执行");
        peoples.test();
    }
}

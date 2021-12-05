package com.example.greeting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@EnableFeignClients
public class HelloWorldClient {

    @Autowired
    private TheClient theClient;

    //这里Feignclient的name为需要需要请求的服务器名称，案例zookeeper中的application-name
    @FeignClient(name = "helloworld")
    interface TheClient {
        @GetMapping(path = "/helloworld")
        String helloWorld();
    }

    public String HelloWorld() {
        String msg = theClient.helloWorld();
        System.out.println("测试:"+msg);
        return msg;
    }
}

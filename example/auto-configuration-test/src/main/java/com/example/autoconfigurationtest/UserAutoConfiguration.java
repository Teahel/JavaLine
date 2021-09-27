package com.example.autoconfigurationtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-09-27
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(UserManager.class)
@EnableConfigurationProperties(UserProperties.class)
public class UserAutoConfiguration {

    @Autowired
    UserProperties userProperties;

    @Bean
    public UserManager getUserManager() {
        return new UserManager(userProperties.getPassword(),userProperties.getUsername());
    }

}

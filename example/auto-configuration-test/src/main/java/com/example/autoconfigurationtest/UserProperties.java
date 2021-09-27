package com.example.autoconfigurationtest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-09-27
 */
@ConfigurationProperties(
        prefix = "user.manager"
)
@Configuration
public class UserProperties {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

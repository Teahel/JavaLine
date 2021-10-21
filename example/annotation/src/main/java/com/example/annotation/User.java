package com.example.annotation;

import lombok.Data;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-10-21
 */

@Data
public class User {

    private String username;

    private String password;

    public User(String username,String password) {

        this.username = username;

        this.password = password;
    }

}

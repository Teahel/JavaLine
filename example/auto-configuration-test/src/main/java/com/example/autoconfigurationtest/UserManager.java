package com.example.autoconfigurationtest;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-09-27
 */

public class UserManager {

    private String username;

    private String password;

    public UserManager(String password,String username) {
        this.username = username;
        this.password = password;
    }

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

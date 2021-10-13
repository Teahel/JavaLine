package com.example.springsecurityoauth2;

import com.google.gson.Gson;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-09-30
 */

public class JwtRequest {

    private String username;
    private String password;

    public JwtRequest(String username, String password) {
        this.setUsername(username);
        this.setPassword(password);
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

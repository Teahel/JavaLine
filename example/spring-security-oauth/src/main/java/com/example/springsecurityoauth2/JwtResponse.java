package com.example.springsecurityoauth2;

import com.google.gson.Gson;
import lombok.Data;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-09-30
 */

@Data
public class JwtResponse {

    /**
     * token
     */
    private String jwttoken;

    /**
     * 超时时间
     */
    private Integer expireTime;

    public JwtResponse(String jwttoken) {
        this.jwttoken = jwttoken;
    }



    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}

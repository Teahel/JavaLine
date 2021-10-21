package com.example.annotation;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-10-21
 */


@Component
@RestController
public class UserController {


    @PostMapping("/user")
    @UserLog
    public Map findUser(String username) {

        System.out.println("测试："+username);

        Map<String,Object> map = new HashMap<>(1);
        User user = new User("heihei","heheh");
        map.put("code",user);
        return map;
    }
}

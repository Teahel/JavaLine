package com.example.autoconfigurationtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-09-27
 */

public class autoConfigurationTest extends AutoConfigurationTestApplicationTests {

    @Autowired
    UserManager userManager;

    @Test
    public void autoConfigurationTest() {
        System.out.println("账户和名字："+userManager.getPassword()+userManager.getUsername());
    }
}

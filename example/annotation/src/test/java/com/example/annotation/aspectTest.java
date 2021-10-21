package com.example.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-10-21
 */

public class aspectTest extends AnnotationApplicationTests {


    @Autowired
    UserController userController;

    @Test
    public void test() {
        userController.findUser("litianjun");
    }


}

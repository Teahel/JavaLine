package com.example.annotation.inherited;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-10-22
 */
@Inherited
@Target(ElementType.TYPE)
public @interface User {
    String username() default "heihei";
}

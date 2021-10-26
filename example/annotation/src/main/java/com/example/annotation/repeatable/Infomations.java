package com.example.annotation.repeatable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-10-22
 */
@Target(ElementType.TYPE)
public @interface Infomations {

    Infomation[] value();
}

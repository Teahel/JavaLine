package com.example.annotation;

import java.lang.annotation.*;
import java.time.LocalDateTime;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-10-21
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserLog {

}

package com.ioc.test.utils;

import java.lang.reflect.Field;

/**
 * java反射
 */
public class ReflectionUtils {

    public static void injectField(Field field, Object obj, Object value) throws IllegalAccessException {
        if(field != null) {
            /**
             * true :private 也允许反射
             */
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

}

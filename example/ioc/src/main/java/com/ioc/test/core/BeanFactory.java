package com.ioc.test.core;

/**
 * bean 工厂接口
 */
public interface BeanFactory {

    /**
     * 获取类名
     * @param name
     * @return
     * @throws Exception
     */
    Object getBean(String name) throws Exception;
}

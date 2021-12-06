package com.ioc.test.bean;

import lombok.Data;

import java.util.List;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-12-06
 */

@Data
public class BeanDefinition {

    private String name;

    private String className;

    private String interfaceName;

    private List<ConstructorArg> constructorArgs;

    private List<PropertyArg> propertyArgs;


}
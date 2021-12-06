package com.ioc.test.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class AnnotationApplcationContext extends BeanFactoryImpl{

    //初始化方法
    public void init(Class clazz){
        loadFile(clazz);
    }

    /**
     * 1、启动类路径下，找到根文件目录。然后递归找到所有文件和文件夹
     * 2、因为Java代码类加载要的是相对路径 例如，com.ioc.test.demo.People.,所以需要替换 "/" 和去掉 .class
     * 3、遍历选找具有@IocComponent的注解类，先类加载，然后动态代理生产实例。
     * 4、再然后遍历寻找每一个实例中的字段是否具有注解@IocAutowired，如果具有注解，利用   String beanName = field.getType().getName();获取上面
     * 类的相对路径寻找是否已经再容器中注入。getBean方法可以获取对象（需优化，字段对象如果没有使用@IocComponent不去生产实例注入。）
     * 5、利用hashmap key唯一，容器只保留一个实例（例如key为com.ioc.test.demo.People）
     * 6、populatebean 方法是将所有的具有
     * @param clazz
     */
    private void loadFile(Class clazz) {
        String classpath = clazz.getResource("/").getPath().replace("/", File.separator);
        File file = new File(classpath);
        ArrayList<String> className = new ArrayList<>();
        getClazzName(file,className);
        for (int i =0;i<className.size();i++) {
            className.set(i,className.get(i).replace(classpath,"").replace("/", ".").replace(".class",""));
        }

        className.forEach(str->{
            createClass(str);
        });

    }

    List<String> getClazzName(File path,List<String> className) {
        try {
            //判断目录是否存在
            if(!path.exists() || !path.isDirectory()){
                throw  new FileNotFoundException("文件地址："+path.getPath());
            }
            String[] files = path.list();
            for (int i=0;i<files.length;i++) {
                File file = new File(path,files[i]);
                if (file.isFile()) {
                    className.add(path+"/"+file.getName());
                } else {
                    getClazzName(file,className);
                }
            }
        }catch (Exception e) {
           e.printStackTrace();
        }
        return null;
    }

}

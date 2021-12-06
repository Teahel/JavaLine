package com.ioc.test.core;

import com.ioc.test.bean.BeanDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnnotationApplcationContext extends BeanFactoryImpl{

    public void init(Class clazz){
        loadFile(clazz);
    }

    private void loadFile(Class clazz) {
        String classpath = clazz.getResource("/").getPath().replace("/", File.separator);
        File file = new File(classpath);
        List<String> className = new ArrayList<>();
        getClazzName(file,className);
        className.forEach(str->{
            String beanName = UUID.randomUUID().toString();
            BeanDefinition definition = new BeanDefinition();
            definition.setName(beanName);
            definition.setClassName(str);
            registerBean(beanName,definition);
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
                    className.add(path+"\\"+file.getName());
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

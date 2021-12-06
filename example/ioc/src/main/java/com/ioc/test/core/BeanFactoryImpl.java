package com.ioc.test.core;


import com.ioc.test.annotation.IocAutowired;
import com.ioc.test.annotation.IocComponent;
import com.ioc.test.utils.BeanUtils;
import com.ioc.test.utils.ClassUtils;
import com.ioc.test.utils.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactoryImpl implements BeanFactory{

    private static final ConcurrentHashMap<String,Object> beanMap = new ConcurrentHashMap<>();

    @Override
    public Object getBean(String name) throws Exception {
        //查找对象是否已经实例化过
        Object bean = beanMap.get(name);
        if(bean != null){
            return bean;
        }
        //如果没有实例化，那就需要调用createBean来创建对象
        bean =  createClass(name);

        //结束返回
        return bean;
    }


    private void populatebean(Object bean) throws Exception {
        Field[] fields = bean.getClass().getSuperclass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                IocAutowired iocAutowired = field.getAnnotation(IocAutowired.class);
                if (iocAutowired != null) {
                    String beanName = field.getType().getName();

                    Object fieldBean = getBean(beanName);
                    if (fieldBean != null) {
                        ReflectionUtils.injectField(field,bean,fieldBean);
                    }
                }

            }
        }
    }

    public Object createClass(String clazz) {
        try {
            Class<?> ca = ClassUtils.loadClass(clazz);
            IocComponent component = ca.getAnnotation(IocComponent.class);
            if(component != null) {
                Object bean = BeanUtils.instanceByCglib(ca,null,null);
                String beanName = ca.getName();
                beanMap.put(beanName,ca.newInstance());
                populatebean(bean);
                return bean;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}

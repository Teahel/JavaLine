
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
     
     代码案例在example ioc中

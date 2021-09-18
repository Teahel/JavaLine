
### AOP
AOP(Aspect-Oriented Programming:面向切面编程分离业务逻辑和系统级服务，使用的业务逻辑开发不用关注日志管理，事务管理等问题。大大的降低了代码的耦合性，提高开发效率，和系统维护性。
系统级服务：日志记录、性能统计、安全控制、事务处理、异常处理等等。

### AOP实现通过动态代理，对于实现接口的类使用jdk动态代理创建类，对于没有实现接口的类使用的是cglib动态代理生成代理对象的子类

### jdk代理案例
jdk代理，使用在实现了接口的类 案例代码如下
```
public interface IUserService {

    void addUser(String username,String password);

    void deleteUser(String username);
}
```

```
public class UserServiceImpl implements IUserService{

    @Override
    public void addUser(String username, String password) {
        System.out.println("账户： "+username+",password： "+password);
    }

    @Override
    public void deleteUser(String username) {

    }
}
```
```
public class JdkProxyUtils implements InvocationHandler {

    private final Object object;

    public JdkProxyUtils(Object target){
        this.object = target;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(object,args);
    }

}
```
 ```
 public class JdkTemplate {

    public static void main(String[] args) {
        IUserService service = new UserServiceImpl();
        IUserService userService = (IUserService) Proxy.newProxyInstance(service.getClass().getClassLoader(), service.getClass().getInterfaces(),new JdkProxyUtils(service));
        userService.addUser("ltj","1223456");
    }

}
 ```


### 描述AOP常用的一些术语有通知(Adivce)、切点（Pointcut）、连接点（Join point）、切面（Aspect）、引入（Introduction）、织入（Weaving）





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

cglib 代理实现案例
```
public class CglibProxy implements MethodInterceptor {

    private Object obj;

    public CglibProxy(Object o) {
        this.obj = o;
    }
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        return method.invoke(obj,objects);

    }
}

```
```
public class Person {

    private String username ;

    private String password = "cglib";

    public void setPerson(String username) {
        this.username = username;
        System.out.println(this.username+"，"+this.password);
    }

    public String getPerson() {
        return username+password;
    }

}
```
```
public class Test {
    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Person.class);
        enhancer.setCallback(new CglibProxy(new Person()));
        Person proxy= (Person)  enhancer.create();
        proxy.setPerson("ltj");
        proxy.getPerson();
    }
}

```
### 描述AOP常用的一些术语有通知(Adivce)、切点（Pointcut）、连接点（Join point）、切面（Aspect）、引入（Introduction）、织入（Weaving）

### 通知（Advice）
通知分为五中类型：

* **Before** ：在方法被调用之前调用
* **After** ：在方法完成后调用通知，无论方法是否执行成功
* **After-returning**： 在方法成功执行之后调用通知
* **After-throwing**：在方法抛出异常后调用通知
* **Around**: 包含了被通知的方法，在被通知的方法调用之前，后调用之后执行自定义的行为

下方代码可以明确表示各个术语作用**完整代码在总目录的example文件夹中**

* 第一部分；待增强的方法
```
@Component
public class User {
    //预增强的方法
    public void addUser(){
        System.out.println("目标方法：添加账户！");
    }
}

```

* 第二部分，配置通知，切面点，连接点（ProceedingJoinPoint继承JoinPoint）
```
@Component
@Aspect
public class UserProxy {

    //针对该类下所有的方法:execution(* com.teahel.aop.User.*(..))
    //针对该类下指定方法：execution(* com.teahel.aop.User.add(..))
    @Pointcut("execution(* com.teahel.aop.User.*(..))")
    public void pointCut() {

    }

    @Before("pointCut()")
    public void before(){
        System.out.println("before......");
    }

    @After("pointCut()")
    public void after(){
        System.out.println("after......");
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("around前......");
        Object o = pjp.proceed();
        System.out.println("around后......");
        return o;
    }

    @AfterReturning("pointCut()")
    public void afterReturning(){
        System.out.println("afterReturning......");
    }

    @AfterThrowing("pointCut()")
    public void afterThrowing(){
        System.out.println("afterThrowing......");
    }

}
```

* 启动执行方法
```
@Test
    public void aopTest(){
        System.out.println("测试开始。。。。。。");
        user.addUser();
        System.out.println("测试结束。。。。。。");
    }
```

* 执行结果，这个执行结果就十分的清晰那个方法执行的先后。
```
测试开始。。。。。。
around前......
before......
目标方法：添加账户！
afterReturning......
after......
around后......
测试结束。。。。。。
```


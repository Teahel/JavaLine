
### SpringBoot 自动装载配置

#### 自动装载原理解析

每个项目都有一个启动类，大概类似如下

```
@SpringBootApplication
public class SpringbeanlifecycleApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringbeanlifecycleApplication.class, args);
	}
}
```

其中关键是@EnableAutoConfiguration 注解,@SpringBootConfiguration实际就是普通的注解标志
```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {

}
```

@EnableAutoConfiguration : 自动装载注解核心 启用 SpringBoot 的自动配置机制

@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) }) ：扫描注解
 注解默认会扫描启动类所在的包下所有的类 ，可以自定义不扫描某些 bean。容器中将排除TypeExcludeFilter和AutoConfigurationExcludeFilter过滤的类。
 
### @EnableAutoConfiguration 解释 启用 SpringBoot 的自动配置机制
```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
        //启用自动配置时可用于覆盖的环境属性。
	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

	Class<?>[] exclude() default {};

	String[] excludeName() default {};

}
```
@AutoConfigurationPackage ：表示将该类所对应的包加入到自动配置。即将MainApplication.java对应的包名加入到自动装配。


关键解释： AutoConfigurationImportSelector
具体继承的类如下

```
public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered {

}

public interface DeferredImportSelector extends ImportSelector {

}

public interface ImportSelector {
    String[] selectImports(AnnotationMetadata var1);
}
```

```
//AutoConfigurationImportSelector 类实现了 ImportSelector接口，也就实现了这个接口中的 selectImports方法，该方法主要用于获取所有符合条件的类的全限定类名，这些类需要被加载到 IoC 容器中。
@Override
public String[] selectImports(AnnotationMetadata annotationMetadata) {
	if (!isEnabled(annotationMetadata)) {
		return NO_IMPORTS;
	}
	//关键方法getAutoConfigurationEntry 
	AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(annotationMetadata);
	return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
}
```

```
protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
               //默认 spring.boot.enableautoconfiguration=true 
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
		// 用于获取EnableAutoConfiguration注解中的 exclude 和 excludeName。
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
		// 通过SpringFactoriesLoader.loadFactoryNames 获取所有自动装载配置类,具体函数内容见下方
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
		//去掉重复的
		configurations = removeDuplicates(configurations);
		//返回任何限制候选配置的排除项。
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		//在configurations中排出exclusions，并打印出来
		checkExcludedClasses(configurations, exclusions);
		// 去掉所有排除项
		configurations.removeAll(exclusions);
		configurations = getConfigurationClassFilter().filter(configurations);
		fireAutoConfigurationImportEvents(configurations, exclusions);
		return new AutoConfigurationEntry(configurations, exclusions);
	}
	
```

```
      // 通过SpringFactoriesLoader.loadFactoryNames 获取所有自动装载配置类
	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
				getBeanClassLoader());
		Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
				+ "are using a custom packaging, make sure that file is correct.");
		return configurations;
	}
```

### 
Spring Boot 启动的时候会通过 @EnableAutoConfiguration 中getAutoConfigurationEntry会使用springfactoriesLoader.loadFactoryNames找到所有jar中的 META-INF/spring.factories 配置文件；
注意Springboot 自身的spring-boot-autoconfigure.jar 下面的META-INF/spring.factories ，如下图所示
![Image of auto_configure](https://github.com/Teahel/JavaLine/blob/main/image/auto_configure.jpg)

 从截图看到org.springframework.boot.autoconfigure.EnableAutoConfiguration为key的value有很多，value都是AutoConfiguration结尾。以逗号隔开。
 这个是Springboot自带的，启动之后会去加载，也可以自己自定义写META-INF/spring.factories文件，然后以org.springframework.boot.autoconfigure.EnableAutoConfiguration为key
 然后具体的value为具体的类。看下图spring-boot-autoconfigure.jar的value类中，Redis为例 AutoConfiguration
 在spring-boot-autoconfigure.jar的META-INF/spring.factories文件中，找到value为 org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration的。idea编译器下，点击这个value就可以进入该类。具体代码如下图

 
 ![redisAutoConguration](https://github.com/Teahel/JavaLine/blob/main/image/redisAutoConfigure.jpg)
 注解提示
 
@Conditional扩展注解 | 作用（判断是否满足当前指定条件）
------------ | -------------
@ConditionalOnJava | 系统的java版本是否符合要求
@ConditionalOnBean | 容器中存在指定Bean；
@ConditionalOnMissingBean | 容器中不存在指定Bean；
@ConditionalOnExpression | 满足SpEL表达式指定
@ConditionalOnClass | 系统中有指定的类
@ConditionalOnMissingClass | 系统中没有指定的类
@ConditionalOnSingleCandidate | 容器中只有一个指定的Bean，或者这个Bean是首选Bean
@ConditionalOnProperty | 系统中指定的属性是否有指定的值
@ConditionalOnResource | 类路径下是否存在指定资源文件
@ConditionalOnWebApplication | 当前是web环境
@ConditionalOnNotWebApplication | 当前不是web环境
@ConditionalOnJnd |JNDI存在指定项



具体解释如下
@Conditional条件满足情况下才会注入ioc容器，上方图片中因为我添加了Redis的jar在pom.xml文件中，所以idea没有提示报错

```
@Configuration(proxyBeanMethods = false)
//是否存在该类
@ConditionalOnClass(RedisOperations.class)
//加载redis的配置，具体下方代码解释
@EnableConfigurationProperties(RedisProperties.class)
//redis的两种实现方式Lettuce  Jedis
@Import({ LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class })
public class RedisAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(name = "redisTemplate")
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

}
```
解释：@EnableConfigurationProperties(RedisProperties.class),下方代码中直接看出，在Springboot的application.properties或者yml文件中直接以spring.redis为前缀，添加其他的端口或者ip等信息
同理自定义方式也是类似做法。
```
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

	/**
	 * Database index used by the connection factory.
	 */
	private int database = 0;

	/**
	 * Connection URL. Overrides host, port, and password. User is ignored. Example:
	 * redis://user:password@example.com:6379
	 */
	private String url;

	/**
	 * Redis server host.
	 */
	private String host = "localhost";

	/**
	 * Login username of the redis server.
	 */
	private String username;

	/**
	 * Login password of the redis server.
	 */
	private String password;

	/**
	 * Redis server port.
	 */
	private int port = 6379;

	/**
	 * Whether to enable SSL support.
	 */
	private boolean ssl;

	/**
	 * Read timeout.
	 */
	private Duration timeout;

	/**
	 * Connection timeout.
	 */
	private Duration connectTimeout;
        .....
	.....
	.....
}
```
Rabbitmq依赖没有添加到pom.xml中，org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration类中注解@ConditionalOnClass({ RabbitTemplate.class, Channel.class })
无法找到该类，所以在自动装载时直接过滤。如下图
![rabbitmqAutoConguration](https://github.com/Teahel/JavaLine/blob/main/image/rabbitmqAutoConguration.jpg)



### 自定义方式
源代码在 example文件夹中，项目名称为 **auto-configuration-test**
上方是Springboot自带，以spring开头的jar.

下面我将自己编写一个

* UserAutoConfiguration
```
//添加相关注解
@Configuration(proxyBeanMethods = false)
//具体注入类
@ConditionalOnClass(UserManager.class)
//配置信息类
@EnableConfigurationProperties(UserProperties.class)
public class UserAutoConfiguration {
    //获取appliaction.properties配置文件的配置信息
    @Autowired
    UserProperties userProperties;
    //@Bean 注入ioc
    @Bean
    public UserManager getUserManager() {
        return new UserManager(userProperties.getPassword(),userProperties.getUsername());
    }

}
```
* 对象实体
```
public class UserManager {

    private String username;

    private String password;

    public UserManager(String password,String username) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

```
* 配置属性，user.manager开头的配置，具备 username 、password属性
```
@ConfigurationProperties(
        prefix = "user.manager"
)
@Configuration
public class UserProperties {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```
在项目resource下的meta-info文件夹中，建立spring.factories配置文件，以下是具体配置内容
org.springframework.boot.autoconfigure.EnableAutoConfiguration为key
value为具体的自动装载类，以AutoConfiguration结尾的类。具备相关@conditional注解
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.autoconfigurationtest.UserAutoConfiguration
```

* 项目的application.properties文件中添加下发配置文件
```
user.manager.password="litianjun"
user.manager.username="litianjun"
```






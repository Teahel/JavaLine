
### SpringBoot 自动装载配置

####  约定优于配置的体现

  * maven的目录结构
    * 默认有resources文件夹存放配置文件
    * 默认打包方式为jar
  * spring-boot-starter-web 中默认包含 spring mvc 相关依赖以及内置的tomcat容器，使得构建一个web应用 更加简单
  * 默认提供application.properties/yml文件
  * 默认通过 spring.profiles.active 属性来决定运行环境时读取的配置文件
  * EnableAutoConfiguration 默认对于依赖的starter进行自动装载

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
其中关键是@SpringBootApplication 注解
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

@EnableAutoConfiguration : 自动装载注解核心

@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
    
    扫描注解，并过滤

    
    
    

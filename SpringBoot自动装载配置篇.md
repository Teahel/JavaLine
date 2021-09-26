
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
//
@Override
public String[] selectImports(AnnotationMetadata annotationMetadata) {
	if (!isEnabled(annotationMetadata)) {
		return NO_IMPORTS;
	}
	AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(annotationMetadata);
	return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
}
```

    
    

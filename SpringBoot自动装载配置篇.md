
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
    

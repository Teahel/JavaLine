package com.example.bd_bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 全局配置。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 允许跨域访问的前端地址模式，多个模式用英文逗号分隔。
     *
     * 示例：
     * app.cors.allowed-origin-patterns=http://localhost:*,http://127.0.0.1:*
     */
    @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOriginPatterns;

    /**
     * 是否允许浏览器携带 Cookie、Authorization 等凭证。
     */
    @Value("${app.cors.allow-credentials:true}")
    private Boolean allowCredentials;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }
}

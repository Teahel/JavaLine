package com.example.trion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trion API")
                        .description("Trino 数据查询接口")
                        .version("v1"));
    }
}

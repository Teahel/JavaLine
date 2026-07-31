package com.example.trion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.example.demo")
public class TrinoConfig {

    @Bean
    public JdbcTemplate trinoJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

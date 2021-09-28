package com.example.springsecurityoauth2;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Log logger = LogFactory.getLog(WebSecurityConfiguration.class);

    @Autowired
    JwtFilterConfiguration jwtFilterConfiguration;



    protected void configure(HttpSecurity http) throws Exception {
        this.logger.debug("Using default configure(HttpSecurity). "
                + "If subclassed this will potentially override subclass configure(HttpSecurity).");

        http.csrf().disable().authorizeRequests().antMatchers("/login").permitAll().and()
                .authorizeRequests().anyRequest().authenticated();
        http.addFilter(jwtFilterConfiguration);


    }



}

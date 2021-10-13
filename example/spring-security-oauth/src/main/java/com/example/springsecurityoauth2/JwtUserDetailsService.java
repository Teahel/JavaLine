package com.example.springsecurityoauth2;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @version 1.0
 * @author： L.T.J
 * @date： 2021-09-30
 */
@Component
public class JwtUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if("test".equals(username)) {
            return new User("test","123456",new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

    }
}

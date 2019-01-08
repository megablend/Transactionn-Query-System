package com.nibss.tqs.config.security;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Created by Emor on 7/1/16.
 */
@Component("customUserDetailService")
@Slf4j
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user;
        user = userRepository.findByEmailLogin(s);
        if (null == user)
            throw new UsernameNotFoundException("Invalid username and/or password");
        return user;
    }
}

package com.nibss.tqs.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.annotation.PostConstruct;

/**
 * Created by Emor on 7/1/16.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {


    @Configuration
    @Slf4j
    @Order(3)
    public static class BaseSecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        private PasswordEncoder passwordEncoder;

        @PostConstruct
        protected void loaded() {
            log.trace("BaseSecurityConfig loaded");
        }

        @Autowired
        @Qualifier("customUserDetailService")
        private UserDetailsService userDetailsService;

        @Autowired
        @Qualifier("customAuthFailureHandler")
        private AuthenticationFailureHandler failureHandler;

        @Autowired
        @Qualifier("customAuthenticationSuccessHandler")
        private AuthenticationSuccessHandler successHandler;

        @Override
        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/resources/**","/static/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().antMatchers("/login","/forgotpassword","/","/swagger*").permitAll()
                    .and()
                    .formLogin()
                    .usernameParameter("username").passwordParameter("password")
                    .loginProcessingUrl("/login").loginPage("/login").defaultSuccessUrl("/")
                    .failureUrl("/login?error").failureHandler(failureHandler).successHandler(successHandler)
                    .and().logout().logoutUrl("/logout").invalidateHttpSession(true).logoutSuccessUrl("/login?logout")
                    .and().authorizeRequests().antMatchers("/changepassword").authenticated() //,"/*"
                    .and().authorizeRequests().antMatchers("/users/changepassword").authenticated()
                    .and().authorizeRequests().antMatchers("/merchantlist/**","/merchantlist*")
                    .hasAnyRole("NIBSS_ADMIN","NIBSS_USER", "BANK_ADMIN","BANK_USER")
                    .and().authorizeRequests().antMatchers("/corporatelounge","/corporatelounge/**").hasAnyRole("CL_USER","CL_ADMIN")
                    .and().authorizeRequests().antMatchers("/organizations/**","/organizations*").hasRole("NIBSS_ADMIN")
                    .and().authorizeRequests().antMatchers("/users*","/users/**").hasRole("ADMIN")
                    .and().authorizeRequests().antMatchers("/billers*","/billers/**").hasRole("NIBSS_ADMIN")
                    .and().authorizeRequests().antMatchers("/ussd*","/ussd/**").hasRole("NIBSS_ADMIN")
                    .and().authorizeRequests().antMatchers("/billing","/billingreport","/billing**","/billingreport/**").authenticated()
                    .and().authorizeRequests().antMatchers("/ebillspay*","/ebillspay/**",
                    "/centralpay*","/centralpay/**","/search/**",
                    "/billpayment*","/billpayment/**","/merchantpayment*","/merchantpayment/**",
                    "/merchantpay*","/merchantpay/**").authenticated()
                    .and().authorizeRequests().antMatchers("/centralpay/billing","/centralpay/sharingconfig").hasRole("NIBSS_ADMIN");
        }
    }


    @Configuration
    @Order(2)
    @Slf4j
    public static class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable();
            http.antMatcher("/api/**").authorizeRequests().anyRequest().permitAll();
        }
    }


    @Configuration
    @Order(1)
    @Slf4j
    public static class CorporateLoungeApiSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable();
            http.antMatcher("/corporateloungeapi/**").authorizeRequests().anyRequest().permitAll();
        }
    }
}

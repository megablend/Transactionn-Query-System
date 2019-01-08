package com.nibss.tqs.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by eoriarewo on 7/18/2016.
 */
@Component("customAuthFailureHandler")
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String EXCEPTION_LABEL = "SPRING_SECURITY_LAST_EXCEPTION";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {


        String loginPath = request.getContextPath() + "/login";
        HttpSession session = request.getSession();

        Exception ex = null;

        if( e instanceof BadCredentialsException)
            ex = new RuntimeException("Invalid username and/or password");
        else if(e instanceof DisabledException)
            ex = new RuntimeException("Your account has been disabled. Kindly contact your Administrator");
        else {
            log.error("An error occurred while authenticating the user",e);
            ex = new RuntimeException("Sorry, you could not be logged in at the moment. Please try again later");
        }
        session.setAttribute(EXCEPTION_LABEL, ex);
        response.sendRedirect(loginPath);
    }
}

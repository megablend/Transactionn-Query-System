package com.nibss.tqs.config.security;

import com.nibss.tqs.ajax.AjaxOrganization;
import com.nibss.tqs.controllers.HomeController;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.core.repositories.ProductRepository;
import com.nibss.tqs.core.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 9/23/2016.
 */
@Component("customAuthenticationSuccessHandler")
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    @Qualifier("tqsExecutorService")
    private ExecutorService executorService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        User user = (User)authentication.getPrincipal();
        if( null == user)
            return;

        AjaxOrganization org;
        try {
            org = (AjaxOrganization)organizationRepository.findByUser(user.getId());
            if( null != org) {
                List<String> productCodes = productRepository.findCodesByOrganization(org.getId());
                org.setProductCodes(productCodes);
                user.setOrganizationInterface(org);

                HttpSession session = httpServletRequest.getSession(false);
                if( null != session) {
                    session.setAttribute(HomeController.USER_SESSION, user);
                }
            }
        } catch (Exception e) {

        }

       Runnable r = () -> {
           try {
                 userRepository.updateLastLoginDateForUser(user.getId());
           } catch (Exception e) {
               log.error("could not update last login date. {}",e.getMessage());
           }
       };

        executorService.execute(r);

        httpServletResponse.sendRedirect( httpServletRequest.getContextPath());
    }
}

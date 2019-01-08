package com.nibss.tqs.controllers;

import com.nibss.tqs.config.QueueConfig;
import com.nibss.tqs.config.security.CurrentUser;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.core.repositories.UserRepository;
import com.nibss.tqs.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by eoriarewo on 7/14/2016.
 */
@Controller
@Slf4j
public class HomeController {

    public static  final String USER_SESSION = "user";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value ="/login", method = RequestMethod.GET)
    public  String login() {
        return "users/login";
    }


    @RequestMapping(value="/", method = RequestMethod.GET)
    public String index(@CurrentUser User user, HttpSession session, HttpServletRequest request) {

        if( null == user)
            return "users/login";

        if( session.getAttribute(USER_SESSION) == null)
            session.setAttribute(USER_SESSION, user);

        String redirectUrl = String.format("redirect:%s",getPathForUser(user,request));
        log.trace("user redirect URL: {}", redirectUrl);
        return redirectUrl;
    }

    @RequestMapping(value="/forgotpassword", method = RequestMethod.GET)
    public String forgotPassword() {
        return "users/forgotpassword";
    }

    @RequestMapping(value = "/forgotpassword", method = RequestMethod.POST)
    public String forgotPassword(Model model, @RequestParam("username") String username, RedirectAttributes redirectAttributes) {

        String forgotPassView = "users/forgotpassword";

        if( username == null != username.trim().isEmpty()) {
            model.addAttribute("error","Kindly supply your username");
            return forgotPassView;
        }

        try {
            User theUser = userRepository.findByEmail(username);
            if( null == theUser) {
                model.addAttribute("error","The email does not exist on this platform");
                return forgotPassView;
            }

            PasswordUtil passwordUtil = new PasswordUtil();
            String newPassword = passwordUtil.generateRandomPassword();
            theUser.setPassword( passwordEncoder.encode(newPassword));
            theUser.setPasswordChanged(false);
            userRepository.save(theUser);
            theUser.setPassword(newPassword);

            jmsTemplate.convertAndSend(QueueConfig.PASSWORD_RESET_QUEUE, theUser);
            redirectAttributes.addFlashAttribute("success","password was successfully reset");
            return "redirect:/forgotpassword";

        } catch(Exception e) {
            log.error(null,e);
            model.addAttribute("error", "Sorry,an error occurred while processing your request. Please try again later");
            return forgotPassView;
        }

    }

    private String getPathForUser(final User user,HttpServletRequest request ) {
        IOrganization org = user.getOrganizationInterface();
        List<Role> roles = user.getRoles();
        /*if( roles == null || roles.isEmpty())
            return "/error";*/
        if(org.getOrganizationType() == Integer.parseInt(OrganizationType.NIBSS)) {
            log.trace("NIBSS user");
            if(request.isUserInRole(Role.ADMIN)) {
                log.trace("user is admin");
                return  "/organizations";
            }
            else
                return "/ebillspay";
        }

        if(org.getProductCodes().contains(Product.EBILLSPAY))
            return "/ebillspay";

        if( org.getProductCodes().contains(Product.CENTRALPAY) )
            return "/centralpay/cards";

        if( org.getProductCodes().contains(Product.USSD_MERCHANT_PAYMENT))
            return  "/merchantpay";

        if( org.getProductCodes().contains(Product.USSD_MERCHANT_PAYMENT))
            return  "/billpayment";

        return "/ebillspay";
    }
}

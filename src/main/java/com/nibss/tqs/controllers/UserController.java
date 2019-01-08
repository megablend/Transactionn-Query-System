package com.nibss.tqs.controllers;

import com.nibss.tqs.ajax.AjaxResponse;
import com.nibss.tqs.config.QueueConfig;
import com.nibss.tqs.config.security.CurrentUser;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.core.repositories.RoleRepository;
import com.nibss.tqs.core.repositories.UserRepository;
import com.nibss.tqs.util.PasswordUtil;
import com.nibss.tqs.validators.UserValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpSession;

/**
 * Created by Emor on 7/9/16.
 */
@Controller
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserValidator userValidator;


    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userValidator);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse createUser(@Valid User user, BindingResult bindingResult, @CurrentUser User loggedInUser,
                            @RequestParam(value = "roles", required = false) Role[] roles) {

        if (user == null)
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED, null);

        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            bindingResult.getAllErrors().stream().map(e -> e.getDefaultMessage()).forEach(e -> errors.add(e));
            return OrganizationController.buildFailedResponse("Validation failed for user creation", errors);
        }

        user.setCreatedBy(loggedInUser.getEmail());
        user.setDateCreated(new Date());
        user.setEnabled(true);

        if (roles != null) {
            user.setRoles(Arrays.asList(roles));
            Stream.of(roles).forEach( r -> r.getUsers().add(user));
        }

//        Role userRole = user.getRoles().get(0);
//        userRole.getUsers().add(user);

        /*
        for all requests, send the org id.
        if it's not present, assume the user is being created by same org and then assign normal user roles to user
         */

        try {
            String password = new PasswordUtil().generateRandomPassword();
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            roleRepo.save(user.getRoles());
//            organizationRepository.save(user.getOrganization());
            user.setPassword(password);

            jmsTemplate.convertAndSend(QueueConfig.USER_CREATION_QUEUE, user); //write user object to mail queue
            return OrganizationController.buildSuccessResponse("User account has been successfully created");
        } catch (Exception e) {
            log.error(null, e);
            return OrganizationController.buildFailedResponse("Sorry, an error occurred while processing your request. Please try again later", null);
        }

    }


    @RequestMapping(value = "", method = RequestMethod.GET)
    public String list(@CurrentUser User user, Model model) {

        List<User> users = new ArrayList<>();
        try {
            users = userRepository.findByOrganization(user.getOrganizationInterface().getId());
        } catch (Exception e) {
            log.error("could not fetch org users", e);
        }
        model.addAttribute("users", users);
        model.addAttribute("organizationId", user.getOrganizationInterface().getId());
        model.addAttribute("isNibss", user.getOrganizationInterface().getOrganizationType() == Integer.parseInt(OrganizationType.NIBSS));

        return "users/list";

    }

    @RequestMapping(value = "/changepassword", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse changePassword(@RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @CurrentUser User loggedInUser, HttpSession session) {
        if (loggedInUser == null)
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED, null);

        if (null == newPassword || null == confirmPassword || confirmPassword.trim().isEmpty() || newPassword.trim().isEmpty())
            return OrganizationController.buildFailedResponse("Parameters cannot be null or empty", null);

        if (!newPassword.equals(confirmPassword))
            return OrganizationController.buildFailedResponse("The Password and its confirmation do not match", null);

        if (!new PasswordUtil().isComplexPassword(newPassword))
            return OrganizationController.buildFailedResponse("Your chosen password does not meet the complexity requirements", null);

        loggedInUser.setPasswordChanged(true);
        try {
            userRepository.updateUserPassword(loggedInUser.getId(), passwordEncoder.encode(newPassword), true);
            session.setAttribute(HomeController.USER_SESSION, loggedInUser);
            return OrganizationController.buildSuccessResponse("Your password was successfully updated!");
        } catch (Exception e) {
            log.error("user could not change password", e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED, null);
        }
    }


    @RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse resetPasswords(@RequestParam(value = "userIds", required = false) Integer[] userIds, Authentication auth) {

        if (auth.getPrincipal() == null)
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED, null);
        if (null == userIds || userIds.length == 0)
            return OrganizationController.buildFailedResponse("No User IDs were selected", null);

        try {
            for (int id : userIds) {
                String newPassword = new PasswordUtil().generateRandomPassword();

                userRepository.updateUserPassword(id, passwordEncoder.encode(newPassword), false);
                User user = userRepository.getBasicUserDetails(id);
                if (null != user) {
                    user.setPassword(newPassword);
                    jmsTemplate.convertAndSend(QueueConfig.PASSWORD_RESET_QUEUE, user);
                }
            }
            return OrganizationController.buildSuccessResponse("The users' passwords were successfully reset");

        } catch (Exception e) {
            log.error("could not reset user passwords", e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED, null);
        }
    }

    @RequestMapping(value = "/updatestatus", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse changeUserStatus(@RequestParam(value = "userIds", required = false) Integer[] userIds, Authentication auth, @RequestParam("action") String action) {
        if (auth.getPrincipal() == null)
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED, null);
        if (null == userIds || userIds.length == 0)
            return OrganizationController.buildFailedResponse("No User IDs were selected", null);

        if (action == null || action.trim().isEmpty())
            return OrganizationController.buildFailedResponse("No action was specified", null);

        try {

            for (int id : userIds) {
                boolean enabled = action.trim().equalsIgnoreCase("enable");
                userRepository.updateUserEnabledStatus(id, enabled, !enabled);
            }
            return OrganizationController.buildSuccessResponse("The users' status(es) have been successfully updated");

        } catch (Exception e) {
            log.error("could not reset user passwords", e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED, null);
        }
    }

}

package com.nibss.tqs.validators;

import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.OrganizationSettingRepository;
import com.nibss.tqs.core.repositories.RoleRepository;
import com.nibss.tqs.core.repositories.UserRepository;
import com.nibss.tqs.ussd.query.MerchantPaymentQueryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Created by eoriarewo on 6/14/2017.
 */
@Component
@Slf4j
public class UserValidator implements Validator {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private OrganizationSettingRepository orgSetRepo;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(User.class);
    }

    @Override
    public void validate(Object o, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "user.firstName", "Please specify the First Name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "user.lastName", "Please specify the Last Name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "user.email", "Please specify the Email");

        User user = (User) o;

        if (!MerchantPaymentQueryHelper.EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            errors.rejectValue("email", "user.email", "Please specify a valid Email");
            return;
        }

        try {
            int emailCount = userRepository.countByEmail(user.getEmail());
            if(emailCount > 0) {
                errors.rejectValue("email", "email.exists", "A user already exists with the Email");
            }
        } catch (Exception e) {
            log.error("could not count by email",e);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (null == authentication) {
            errors.rejectValue("organization", "unauthorized", "You are not permitted to carry out this function. Kindly login to try again");
            return;
        }


        User loggedInUser = (User) authentication.getPrincipal();

        Organization org = user.getOrganization();
        if (org.getOrganizationType() != Integer.parseInt(OrganizationType.NIBSS)) {

            OrganizationSetting setting = orgSetRepo.findByOrganization(org.getId());
            if (null == setting) {
                errors.rejectValue("organization", "org.settings", "Organization Settings are yet to be configured for organization");
            } else {
                String roleName = null;
                int maxCount;
                if (loggedInUser.getOrganizationInterface().getId() == user.getOrganization().getId()) {
                    roleName = Role.USER;
                    maxCount = org.getOrganizationSetting().getNoOfOperators();
                } else {
                    roleName = Role.ADMIN;
                    maxCount = org.getOrganizationSetting().getNoOfAdmins();
                }

                try {
                    int count = userRepository.countUserRoleByOrganization(org.getId(), roleName);
                    log.trace("count for role {} is {}", roleName, count);
                    if ((count + 1) > maxCount) {
                        errors.rejectValue("roles", "user.exceeds", "You have exceeded the user creation quota");
                        return;
                    }
                } catch (Exception e) {
                    log.error("could not get max role count", e);
                }

                Role userRole = roleRepo.findByName(roleName);
                log.trace("User role is {}", userRole);
                user.getRoles().add(userRole);
                userRole.getUsers().add(user);
            }

        }
    }
}

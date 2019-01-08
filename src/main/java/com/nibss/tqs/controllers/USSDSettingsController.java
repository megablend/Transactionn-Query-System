package com.nibss.tqs.controllers;

import com.nibss.tqs.ajax.AjaxResponse;
import com.nibss.tqs.core.entities.MerchantPaymentSharingConfig;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.MerchantPaymentSharingConfigRepository;
import com.nibss.tqs.ussd.dto.UssdBiller;
import com.nibss.tqs.ussd.dto.UssdFeeSharingConfig;
import com.nibss.tqs.ussd.repositories.UssdBillerRepository;
import com.nibss.tqs.ussd.repositories.UssdFeeSharingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 8/15/2016.
 */
@Controller
@RequestMapping("/ussd")
@Slf4j
public class USSDSettingsController {

    @Autowired
    private MerchantPaymentSharingConfigRepository configRepository;

    @Autowired
    private UssdBillerRepository ussdBilllerRepository;

    @Autowired
    private UssdFeeSharingRepository ussdFeeSharingRepository;


    @RequestMapping(value = "",method = RequestMethod.GET)
    public String index(Model model) {
        /*
        get USSD billers
        add tab that shows the ussd billing config maintained
        add tab to show details
         */
        try {
            List<MerchantPaymentSharingConfig> configs = configRepository.findAll();
            if( null != configs && !configs.isEmpty())
                model.addAttribute("merchantPaymentConfig", configs.get(0));
            else
                model.addAttribute("merchantPaymentConfig", new MerchantPaymentSharingConfig());
        } catch (Exception e) {
            model.addAttribute("merchantPaymentConfig", new MerchantPaymentSharingConfig());
            log.error("could not get merchant payment sharing config",e);
        }

        try {
            model.addAttribute("ussdBillers", ussdBilllerRepository.findAll());
        } catch (Exception e) {
            log.error("could not load USSD billers",e);
            model.addAttribute("ussdBillers", new ArrayList<>(0));
        }

        try {
            model.addAttribute("ussdBillingConfig", ussdFeeSharingRepository.findAll());
        } catch (Exception e) {
            log.error("could not load USSD billing config",e);
            model.addAttribute("ussdBillingConfig", new ArrayList<>(0));
        }


        return "ussd/list";
    }

    @RequestMapping(value = "/merchantpaymentconfig", method = RequestMethod.POST)
    public @ResponseBody AjaxResponse updateMerchantPaymentConfig(@Valid MerchantPaymentSharingConfig config, BindingResult bR, Authentication auth) {
        if( auth.getPrincipal() == null)
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED,null);
        if(bR.hasErrors()) {
            List<String> errors = bR.getAllErrors().stream().map( s -> s.getDefaultMessage()).collect(Collectors.toList());
            return  OrganizationController.buildFailedResponse("Invalid object request",errors);
        }

        User loggedInUser = (User) auth.getPrincipal();
        try {

            List<MerchantPaymentSharingConfig> configs = configRepository.findAll();
            if( null != configs && !configs.isEmpty()) {
                config.setId( configs.get(0).getId());
                config.setDateModified( new Date());
                config.setCreatedBy(loggedInUser.getEmail());
            } else {
                config.setCreatedBy( loggedInUser.getEmail());
                config.setDateCreated(new Date());
            }


            configRepository.save(config);
            return OrganizationController.buildSuccessResponse("Your request was successfully processed");

        } catch (Exception e) {
            log.error("could not update merchant payment sharing config",e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED,null);
        }
    }

    @RequestMapping(value = "/ussdsharingconfig", method = RequestMethod.POST)
    public @ResponseBody AjaxResponse ussdBillingConfiguration(@Valid UssdFeeSharingConfig config, BindingResult bR, Authentication auth) {

        if( null == auth.getPrincipal())
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED,null);

        if( bR.hasErrors()) {
            List<String> errors = bR.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
            OrganizationController.buildFailedResponse("Validation failed",errors);
        }

//        User loggedInUser = (User)auth.getPrincipal();

        UssdBiller biller  = config.getUssdBiller();
        if( null == biller)
            return OrganizationController.buildFailedResponse("No biller found for this Sharing Configuration",null);

        if( biller.getFeeSharingConfig() != null)
          return OrganizationController.buildFailedResponse("A sharing config has already been maintained for the merchant",null);

        biller.setFeeSharingConfig(config);

        try {
            ussdBilllerRepository.save(biller);
            return OrganizationController.buildSuccessResponse("USSD Billing Configuration was successfully saved");
        } catch (Exception e) {
            log.error("could not save USSD billing configuration",e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED,null);
        }
    }

}

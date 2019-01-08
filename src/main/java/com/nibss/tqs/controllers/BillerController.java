package com.nibss.tqs.controllers;

import com.nibss.tqs.ajax.AjaxResponse;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ebillspay.dto.BillerSetting;
import com.nibss.tqs.ebillspay.dto.EbillsBillingConfiguration;
import com.nibss.tqs.ebillspay.dto.EbillsPayTransactionFee;
import com.nibss.tqs.ebillspay.repositories.BillerRepository;
import com.nibss.tqs.ebillspay.repositories.EbillsBillingConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 8/9/2016.
 */
@Controller
@RequestMapping("/billers")
@Slf4j
public class BillerController {

    @Autowired
    private BillerRepository billerRepository;

    @Autowired
    private EbillsBillingConfigurationRepository ebillsBillingConfigurationRepository;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {

        try {
            model.addAttribute("billers", billerRepository.searchAll());
            model.addAttribute("ebillspayConfig", ebillsBillingConfigurationRepository.findAllForRendering());
        } catch (Exception e) {
            log.error("could not get billers",e);
            model.addAttribute("billers", new ArrayList<>(0));
        }
        return "billers/list";
    }

    @RequestMapping(value = "/{billerId}", method = RequestMethod.GET)
    public String details(@PathVariable("billerId") int billerId, Model model) {

        try{
            Biller biller = billerRepository.findOne(billerId);
            if( null == biller)
                return "redirect:/404";
            model.addAttribute("biller", biller);
            return "billers/details";
        } catch (Exception e) {
            log.error("Could not get details for biller {}",billerId,e);
            return "redirect:/404";
        }
    }

    @RequestMapping(value = "/ebillssharingconfig", method = RequestMethod.POST)
    public @ResponseBody AjaxResponse billingConfiguration(@Valid EbillsBillingConfiguration config, BindingResult bR, Authentication auth) {

        if( null == auth.getPrincipal())
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED,null);

        if( bR.hasErrors()) {
            List<String> errors = bR.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
            OrganizationController.buildFailedResponse("Validation failed",errors);
        }

//        User loggedInUser = (User)auth.getPrincipal();

        Biller biller  = config.getBiller();
        if( null == biller)
            return OrganizationController.buildFailedResponse("No biller found for this Sharing Configuration",null);

        if( null != biller.getEbillsBillingConfigurations())
            return OrganizationController.buildFailedResponse("A sharing config has already been maintained for the biller",null);

        biller.setEbillsBillingConfigurations(config);

        try {
            billerRepository.save(biller);
            return OrganizationController.buildSuccessResponse("E-bills Billing Configuration was successfully saved");
        } catch (Exception e) {
            log.error("could not save ebills billing configuration",e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED,null);
        }
    }

    @RequestMapping(value = "/{billerId}/settings",method = RequestMethod.POST)
    public  @ResponseBody AjaxResponse settings(@PathVariable int billerId, @Valid BillerSetting billerSetting, BindingResult bR, Authentication auth) {
        if( null == auth.getPrincipal())
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED,null);

        if( bR.hasErrors()) {
            List<String> errors = bR.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
            OrganizationController.buildFailedResponse("Validation failed",errors);
        }
        User loggedInUser = (User)auth.getPrincipal();
        try {
            Biller biller = billerRepository.findOne(billerId);
            if( null == biller)
                return OrganizationController.buildFailedResponse("Requested biller could not be found",null);
            if( null == biller.getBillerSetting()) {
                billerSetting.setCreatedBy(loggedInUser.getEmail());
                biller.setBillerSetting(billerSetting);
                billerSetting.setBiller(biller);
            } else {
                billerSetting.setId( biller.getBillerSetting().getId());
                biller.setBillerSetting(billerSetting);
            }
            if( null == billerSetting.getCreatedBy())
                billerSetting.setCreatedBy(loggedInUser.getEmail());

            billerRepository.save(biller);
            return OrganizationController.buildSuccessResponse("Biller Setting was saved successfully");
        } catch (Exception e) {
            log.error("could not update biller setting",e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED,null);
        }
    }

    @RequestMapping(value="/{billerId}/ebillspaytransactionfee", method = RequestMethod.POST)
    public @ResponseBody AjaxResponse ebillsPayTransactionFee(@Valid EbillsPayTransactionFee transactionFee, BindingResult bR, @PathVariable int billerId, Authentication auth) {
        if( auth.getPrincipal() == null)
            return  OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED,null);
        if( bR.hasErrors()) {
            List<String> errors  = bR.getAllErrors().stream().map( e -> e.getDefaultMessage()).collect(Collectors.toList());
            return OrganizationController.buildFailedResponse("validation failed",errors);
        }

        User loggedInUser = (User)auth.getPrincipal();

        try {
            Biller biller = billerRepository.findOne(billerId);
            if( null == biller)
                return OrganizationController.buildFailedResponse("Requested biller could not be found",null);
            if( null == biller.getEbillsPayTransactionFee() ) {
                transactionFee.setCreatedBy(loggedInUser.getEmail());
                biller.setEbillsPayTransactionFee(transactionFee);
            } else {
                transactionFee.setId( biller.getEbillsPayTransactionFee().getId());
                transactionFee.setDateModified(new Date());
                transactionFee.setModifiedBy(loggedInUser.getEmail());
                biller.setEbillsPayTransactionFee(transactionFee);
            }
            billerRepository.save(biller);
            return OrganizationController.buildSuccessResponse("e-BillsPay Transaction Fee successfully saved");
        } catch (Exception e) {
            log.error("could not update ebillspay transaction fee",e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED,null);
        }
    }
}

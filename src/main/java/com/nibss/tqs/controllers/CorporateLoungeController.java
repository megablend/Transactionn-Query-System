package com.nibss.tqs.controllers;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountStatus;
import com.nibss.corporatelounge.dto.BillingSetting;
import com.nibss.corporatelounge.dto.Organization;
import com.nibss.cryptography.AESKeyGenerator;
import com.nibss.cryptography.IVKeyPair;
import com.nibss.tqs.ajax.AjaxResponse;
import com.nibss.tqs.config.QueueConfig;
import com.nibss.tqs.config.security.CurrentUser;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.corporatelounge.ajax.AccountDto;
import com.nibss.tqs.corporatelounge.ajax.AccountStatusChangeDto;
import com.nibss.tqs.corporatelounge.repositories.AccountRepository;
import com.nibss.tqs.corporatelounge.repositories.CorporateLoungeBillingSettingRepository;
import com.nibss.tqs.corporatelounge.service.AccountService;
import com.nibss.tqs.corporatelounge.service.ClientService;
import com.nibss.tqs.corporatelounge.validators.BillingSettingValidator;
import com.nibss.tqs.corporatelounge.validators.OrganizationValidator;
import com.nibss.tqs.ussd.query.MerchantPaymentQueryHelper;
import com.nibss.tqs.validators.UserValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by eoriarewo on 4/24/2017.
 */
@Controller
@RequestMapping("/corporatelounge")
//@Profile("staging")
@Slf4j
public class CorporateLoungeController {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private OrganizationValidator organizationValidator;

    @Autowired
    private AESKeyGenerator aesKeyGenerator;


    @Autowired
    private CorporateLoungeBillingSettingRepository billingSettingRepository;

    @Autowired
    private BillingSettingValidator billingSettingValidator;

    @InitBinder("organization")
    protected void orgInitBinder(WebDataBinder binder) {
        binder.setValidator(organizationValidator);
    }

    @InitBinder("billingSetting")
    protected void billingInitBinder(WebDataBinder binder) {
        binder.setValidator(billingSettingValidator);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        List<Organization> organizations = null;
        try {
            organizations = clientService.findAll();
        } catch (Exception e) {
            log.error("could not get clients from db", e);
            organizations = new ArrayList<>(0);
        }
        model.addAttribute("organizations", organizations);
        return "clounge/organizations";
    }

    /**
     * called to profile new clients to the corporate lounge service
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse addClient(@Valid Organization organization, BindingResult bR, @CurrentUser User user) {

        if (null == user)
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED, null);
        AjaxResponse response = new AjaxResponse();
        if (bR.hasErrors()) {
            List<String> errors = bR.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
            return OrganizationController.buildFailedResponse("Data validation failed", errors);
        }

        IVKeyPair pair = aesKeyGenerator.generateKeyPair();
        organization.setSecretKey(pair.getSecretKey());
        organization.setIvKey(pair.getIvKey());
        organization.setCreatedBy(user.getEmail());

        try {
            clientService.save(organization);
            jmsTemplate.convertAndSend(QueueConfig.CL_NEW_CLIENT, organization);
            response.setStatus(AjaxResponse.SUCCESS);
        } catch (Exception e) {
            log.error("could not save client", e);
        }

        return response;
    }

    /**
     * returns info for the organization, also the accounts
     *
     * @param orgId
     * @param model
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getOrganization(@PathVariable("id") long orgId, Model model) {

        Organization org = null;
        try {
            org = clientService.findById(orgId);
        } catch (Exception e) {
            log.error("could not get CL organization", e);
        }

        if (null == org)
            return "errors/404";
        Set<AccountDto> accounts = null;
        try {
            accounts = accountService.findAllByOrganizationForView(orgId);
        } catch (Exception e) {
            accounts = new HashSet<>();
        }

        model.addAttribute("organization", org);
        model.addAttribute("accounts", accounts);
        return "clounge/details";
    }


    @RequestMapping(value = "/accounts/updatestatus", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse updateAcctStatus(@RequestParam("acctIds") long[] acctIds, @RequestParam("action") AccountStatus accountStatus) {
        if (null == acctIds || acctIds.length == 0)
            return OrganizationController.buildFailedResponse("No account was selected", null);
        try {
            LocalDate date = LocalDate.now();
            Date dateActive = accountStatus == AccountStatus.APPROVED ? Timestamp.valueOf(date.atStartOfDay()) : null;
            Date expiryDate = accountStatus == AccountStatus.APPROVED ? Timestamp.valueOf(date.atStartOfDay().plusYears(1)) : null;
            for (Long i : acctIds) {
                accountService.updateAccountStatusAndDates(i,accountStatus,dateActive,expiryDate);
            }

            AccountStatusChangeDto accountStatusChangeDto = new AccountStatusChangeDto(acctIds, accountStatus);
            jmsTemplate.convertAndSend(QueueConfig.CL_ACCOUNT_STATUS_CHANGE, accountStatusChangeDto);
            return OrganizationController.buildSuccessResponse("Account Statuses have been successfully updated");
        } catch (Exception e) {
            log.error("could not update acct statuses", e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED, null);
        }
    }

    @RequestMapping(value = "/accounts/email", method = RequestMethod.POST)
    public @ResponseBody AjaxResponse updateAccountEmails(@RequestParam("acctIds") long[] acctIds, @RequestParam("email") String email) {
        if( null == acctIds || acctIds.length ==0) {
            return OrganizationController.buildFailedResponse("No account was selected", null);
        }

        if( null == email || email.trim().isEmpty() || !MerchantPaymentQueryHelper.EMAIL_PATTERN.matcher(email).matches())
            return OrganizationController.buildFailedResponse("Please specify a valid email",null);

        try {
            for( long i : acctIds)
                accountService.updateAccountEmail(i, email);

            return OrganizationController.buildSuccessResponse("Account emails have been successfully updated");
        } catch (Exception e) {
            log.error("could not update acct email",e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED, null);
        }
    }


    @RequestMapping( value = "/setting", method = RequestMethod.GET)
    public String billingSetting( Model model) {
        BillingSetting billingSetting = null;
        try {
            billingSetting = billingSettingRepository.findFirstByOrderById();
        } catch (Exception e ) {
            log.error(  "could not get billing  setting", e);
        }

        if( null == billingSetting)
            billingSetting = new BillingSetting();

        model.addAttribute("setting", billingSetting);
        return  "clounge/setting";
    }


    @RequestMapping( value = "/setting", method = RequestMethod.POST)
    public @ResponseBody AjaxResponse billingSetting(@Valid BillingSetting billingSetting, BindingResult bRes) {
        if( bRes.hasErrors()) {
            List<String> errors = bRes.getAllErrors()
                    .stream().map( e -> e.getDefaultMessage())
                    .collect(Collectors.toList());
            return OrganizationController.buildFailedResponse("Validation failed", errors);
        }

        try {
            BillingSetting existing = billingSettingRepository.findFirstByOrderById();
            if( null != existing)
                billingSetting.setId(existing.getId());
            else
                billingSetting.setId(0);
            billingSettingRepository.save(billingSetting);
            return OrganizationController.buildSuccessResponse("Billing Setting successfully updated");
        } catch(Exception e) {
            log.error("could not get billing setting from db",e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED, null);
        }
    }

}

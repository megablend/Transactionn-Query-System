package com.nibss.tqs.corporatelounge.validators;

import com.nibss.corporatelounge.dto.Organization;
import com.nibss.tqs.corporatelounge.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Component
//@Profile("staging")
public class OrganizationValidator implements Validator {

    @Autowired
    private ClientService clientService;

    public static final Pattern VALID_EMAIL_ADDRESS =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static final Pattern INST_CODE = Pattern.compile("^\\d{6}$");

    public static final Pattern ACCT_NO = Pattern.compile("^\\d{10}$");

    @Value("${cl.maxBEAccountsPerRequest}")
    private int maxAccountsForBalance;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(Organization.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"name", "org.name.empty","Please specify Name");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"institutionCode",
                "org.instCode.empty", "Please specify Institution Code");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"emails",
                "org.email.empty","Please specify Emails");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "debitAccountNumber", "act.no.empty",
                "Please specify Debit Account Number");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "debitAccountName", "act.name.empty",
                "Please specify Debit Account Name");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "debitBankCode", "acct.bankcode.empty",
                "Debit Bank Code was not specified");


        Organization target = (Organization)o;
        if( target.getMaxRequestSize() <= 0) {
            errors.rejectValue("maxRequestSize","org.max.reqsize","Max. Request Size should be greater than zero");
        }

        if(target.getMaxRequestSize() > maxAccountsForBalance) {
            errors.rejectValue("maxRequestSize",
                    "org.max.requestSize", "Max. Request Size cannot be greater than " + maxAccountsForBalance);
        }

        String instCode = target.getInstitutionCode();

        if( null != instCode) {
            if( !INST_CODE.matcher(instCode).matches()) {
                errors.rejectValue("institutionCode", "org.instCode", "Institution Code should be 6-digits");
                return;
            }
        }

        if( null != target.getDebitAccountNumber()) {
                if(!ACCT_NO.matcher(target.getDebitAccountNumber()).matches())
                    errors.rejectValue("debitAccountNumber", "debitAcct.invalid", "Debit Account Number is not valid");
        }
        if( null != target.getDebitAccountName()) {
            if( target.getDebitAccountName().length() <3 || target.getDebitAccountName().length() > 100)
                errors.rejectValue("debitAccountName", "acct.name.invalid", "Please enter a valid Debit Account Name");
        }

        if( null != target.getDebitBankCode()) {
            if(target.getDebitBankCode().trim().length() != 3)
                errors.rejectValue("debitBankCode", "bankcode.invalid", "Please enter a valid CBN Bank Code");
        }

        String email = target.getEmails();

       if( null != email && !email.trim().isEmpty()) {
           List<String> emails = Stream.of(email.split(",")).filter( s -> !s.isEmpty())
                   .map( s -> Stream.of( s.split(";") ) ).flatMap( s -> s.distinct()).collect(Collectors.toList());

           if( emails.stream().map( s -> VALID_EMAIL_ADDRESS.matcher(s).matches()).noneMatch(bool -> bool) ) {
               errors.rejectValue("emails", "org.email.invalid","Emails contains an invalid Email. Please rectify");
           }
       }


        if( target.getId()  == 0) {
            try{
                long count = clientService.countByInstitutionCode(target.getInstitutionCode());
                if(count > 0) {
                    errors.rejectValue("institutionCode","org.instCode.assigned","Institution Code has already been assigned");
                    return;
                }
            } catch(Exception e) {

            }
        }
    }
}

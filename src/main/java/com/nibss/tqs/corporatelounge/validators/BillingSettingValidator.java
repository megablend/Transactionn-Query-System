package com.nibss.tqs.corporatelounge.validators;

import com.nibss.corporatelounge.dto.BillingSetting;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

/**
 * Created by eoriarewo on 10/19/2017.
 */
@Component
public class BillingSettingValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(BillingSetting.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "annualFee", "annualfee.empty", "Please specify Annual Fee");
        ValidationUtils.rejectIfEmpty(errors, "perTransactionFee",
                "transactionFee.empty", "Please specify Per-Transaction Fee");

        ValidationUtils.rejectIfEmpty(errors,"nibssShare", "nibssShare.empty", "Please specify NIBSS Share");
        ValidationUtils.rejectIfEmpty(errors, "bankShare", "bankShare.empty", "Please specify Bank Share");


        BillingSetting setting = (BillingSetting)o;
        validateNumber(setting.getAnnualFee(), "annualFee", errors, "Annual Fee must be greater than zero");
        validateNumber(setting.getPerTransactionFee(), "perTransactionFee", errors,
                "Per-Transaction Fee must be greater than zero");
        validateNumber(setting.getNibssShare(), "nibssShare", errors, "NIBSS Share must be greater than zero");
        validateNumber(setting.getBankShare(), "bankShare", errors, "Bank Share must be greater than zero");

        if( null != setting.getNibssShare() && null != setting.getBankShare()) {
            if( BigDecimal.ONE.compareTo( setting.getBankShare().add(setting.getNibssShare())) != 0)
                errors.rejectValue("bankShare", "percentage.invalid",
                        "The sum of Bank Share and NIBSS Share must be 1 since they are percentages");
        }

    }

    private void validateNumber(BigDecimal value, String fieldName, Errors errors, String message) {
        if( null != value) {
            if( value.compareTo(BigDecimal.ZERO) <= 0)
                errors.rejectValue(fieldName, fieldName+".invalid", message);
        }
    }
}

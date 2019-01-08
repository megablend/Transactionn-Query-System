package com.nibss.tqs.formatters;

import com.nibss.tqs.centralpay.dto.CpayMerchant;
import com.nibss.tqs.centralpay.repositories.CpayMerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by eoriarewo on 9/14/2016.
 */
@Component
public class CpayMerchantFormatter implements Formatter<CpayMerchant> {

    @Autowired
    private CpayMerchantRepository merchantRepository;

    @Override
    public CpayMerchant parse(String s, Locale locale) throws ParseException {
        return merchantRepository.findByMerchantCode(s);
    }

    @Override
    public String print(CpayMerchant cpayMerchant, Locale locale) {
        return cpayMerchant.getMerchantCode();
    }
}

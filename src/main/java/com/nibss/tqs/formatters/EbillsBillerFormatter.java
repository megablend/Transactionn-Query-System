package com.nibss.tqs.formatters;

import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ebillspay.repositories.BillerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by eoriarewo on 8/12/2016.
 */
@Component("ebillsBillerFormatter")
public class EbillsBillerFormatter implements Formatter<Biller> {

    @Autowired
    private BillerRepository billerRepository;

    @Override
    public Biller parse(String s, Locale locale) throws ParseException {
        int billerId = Integer.parseInt(s.trim());
        return  billerRepository.findOne(billerId);
    }

    @Override
    public String print(Biller biller, Locale locale) {
       return Integer.toString(biller.getId());
    }
}

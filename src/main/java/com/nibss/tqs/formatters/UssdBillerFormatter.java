package com.nibss.tqs.formatters;

import com.nibss.tqs.ussd.dto.UssdBiller;
import com.nibss.tqs.ussd.repositories.UssdBillerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by eoriarewo on 8/30/2016.
 */
@Component
public class UssdBillerFormatter implements Formatter<UssdBiller> {

    @Autowired
    private UssdBillerRepository ussdBillerRepository;

    @Override
    public UssdBiller parse(String s, Locale locale) throws ParseException {
        return ussdBillerRepository.findOne(Integer.parseInt(s));
    }

    @Override
    public String print(UssdBiller ussdBiller, Locale locale) {
        return Integer.toString(ussdBiller.getId());
    }
}

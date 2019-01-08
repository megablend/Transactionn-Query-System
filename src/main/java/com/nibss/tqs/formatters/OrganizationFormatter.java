package com.nibss.tqs.formatters;

import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.repositories.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by eoriarewo on 6/14/2017.
 */
@Component
public class OrganizationFormatter implements Formatter<Organization> {

    @Autowired
    private OrganizationService organizationService;

    @Override
    public Organization parse(String s, Locale locale) throws ParseException {
        return organizationService.findOne(Integer.parseInt(s));
    }

    @Override
    public String print(Organization organization, Locale locale) {
        return Integer.toString(organization.getId());
    }
}

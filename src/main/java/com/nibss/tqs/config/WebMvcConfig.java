package com.nibss.tqs.config;

import com.nibss.tqs.centralpay.dto.CpayMerchant;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.entities.Role;
import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ussd.dto.UssdBiller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.text.Normalizer;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {



    @Autowired
    private Formatter<Role> roleFormatter;

    @Autowired
    private Formatter<Organization> organizationFormatter;

    @Autowired
    private Formatter<Biller> billerFormatter;

    @Autowired
    private Formatter<Product> productFormatter;

    @Autowired
    private Formatter<UssdBiller> ussdBillerFormatter;

    @Autowired
    private Formatter<CpayMerchant> cpayMerchantFormatter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(roleFormatter);
        registry.addFormatter(organizationFormatter);
        registry.addFormatter(billerFormatter);
        registry.addFormatter(productFormatter);
        registry.addFormatter(ussdBillerFormatter);
        registry.addFormatter(cpayMerchantFormatter);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}

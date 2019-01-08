package com.nibss.tqs.config;

import com.nibss.tqs.billing.BillingNotification;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by eoriarewo on 7/11/2016.
 */
@Component
@Slf4j
public class BillingQueueListeners {


    @Autowired
    @Qualifier("freeMarkerConfig")
    private freemarker.template.Configuration fmConfig;

    private static final String WEEKLY_BILLING_TEMPLATE = "weeklyBilling.html";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private MailHelper mailHelper;

    @JmsListener(destination = QueueConfig.WEEKLY_BILLING_QUEUE)
    public void notifyOfBilling(BillingNotification billingNotification) {

        List<String> billingMailList = appSettings.billingMailGroup();
        if( null == billingMailList || billingMailList.isEmpty())
            return;

        String mailBody = null;

        try {
            Template mailTemplate = fmConfig.getTemplate(WEEKLY_BILLING_TEMPLATE);
            Map<String,Object> map = new HashMap<>();
            map.put("product",billingNotification.getProduct());
            map.put("period",billingNotification.getPeriod());

            StringWriter writer = new StringWriter();
            mailTemplate.process(map,writer);
            mailBody = writer.toString();
        } catch (IOException e) {
            log.error("could not get mail template",e);
        } catch (TemplateException e) {
            log.error("Could not process template",e);
        }

        if( null == mailBody)
            return;


        try {
            List<Resource> resources = new ArrayList<>();
            resources.add( new FileSystemResource(billingNotification.getBillingFilePath()));
            mailHelper.sendMail("Product Billing", billingMailList, mailBody,resources );
            log.trace("mail sent");
        } catch (Exception e) {
            log.error("could not send mail",e);
        }

    }

}

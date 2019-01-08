package com.nibss.tqs.config;

import com.nibss.tqs.core.entities.User;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Emor on 8/1/2016.
 */
@Component
@Slf4j
public class MailQueueListeners {

    @Autowired
    @Qualifier("freeMarkerConfig")
    private freemarker.template.Configuration fmConfig;

    private static final String USER_CREATION = "userCreation.html";

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private MailHelper mailHelper;

    @JmsListener(destination = QueueConfig.USER_CREATION_QUEUE,concurrency = "3-5")
    public  void sendNewUserMail(final User user) {

        String mailBody = null;

        try {
            Template mailTemplate = fmConfig.getTemplate(USER_CREATION);
            Map<String,Object> map = new HashMap<>();
            map.put("user",user);
            map.put("url",appSettings.applicationUrl());

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

        sendMail(mailHelper,"User Account Created", mailBody, user.getEmail(),null);

    }

    @JmsListener(destination = QueueConfig.PASSWORD_RESET_QUEUE,concurrency = "3")
    public void setPasswordResetMail(User user) {

        String mailBody = null;

        try {
            Template template = fmConfig.getTemplate("passwordReset.html");
            Map<String,Object> map = new HashMap<>();
            map.put("user", user);
            map.put("url",appSettings.applicationUrl());

            StringWriter writer = new StringWriter();
            template.process(map,writer);

            mailBody = writer.toString();
        } catch (IOException e) {
           log.error("could not load template",e);
        } catch(TemplateException e) {
            log.error("could not process template",e);
        }

        if( null == mailBody)
            return;

        sendMail(mailHelper,"Password Reset", mailBody, user.getEmail(), null);

    }

     static void sendMail(MailHelper mailHelper, String subject, String body, String email, List<Resource> resources) {
         synchronized (new Object()) {
             try {
               List<String> items = splitter(email,",",";");

                 mailHelper.sendMail(subject, Arrays.asList(email),body,resources);
                 log.trace("mail sent to server");
             } catch (Exception e) {
                 log.error("could not send email",e);
             }
         }
     }

    static void sendMail(MailHelper mailHelper,String subject, String body, String email, List<String> copy, List<Resource> resources) {
        synchronized (new Object()) {
            try {
                List<String> items = splitter(email,",",";");

                mailHelper.sendMail(subject, Arrays.asList(email),body,resources, copy);
                log.trace("mail sent to server");
            } catch (Exception e) {
                log.error("could not send email",e);
            }
        }
    }

     private static List<String> splitter(String main, String delimiter, String secondaryDelimiter) {
        String[] parts = main.split(delimiter);
        return Stream.of(parts).map( s -> s.trim()).filter( s -> !s.isEmpty())
                .map( s -> Stream.of(s.split(secondaryDelimiter))).flatMap( s -> s.distinct()).collect(Collectors.toList());
     }

}

package com.nibss.tqs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

/**
 * Created by eoriarewo on 8/29/2016.
 */
@Component
@Scope("prototype")
public class MailHelper {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    ApplicationSettings appSettings;

    private static final String LOGO = "nibssLogo";

    public void sendMail(String subject, List<String> to, String body, List<Resource> resources) throws  Exception {
        synchronized (new Object()) {
            MimeMessage message = mailSender.createMimeMessage();
            getBasicMessage(message, subject,to, body, resources);
            mailSender.send(message);
        }
    }


    public void sendMail(String subject, List<String> to, String body, List<Resource> resources, List<String> ccList) throws  Exception {
        synchronized (new Object()) {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = getBasicMessage(message, subject, to, body, resources);
            if( null != ccList && !ccList.isEmpty())
                helper.setCc( ccList.toArray(new String[0]));
            mailSender.send(message);
        }
    }

    private MimeMessageHelper getBasicMessage(MimeMessage message, String subject, List<String> to, String body, List<Resource> resources) throws Exception {

        MimeMessageHelper msgHelper = new MimeMessageHelper(message,true);
        msgHelper.setText(body,true);
        msgHelper.setTo(to.toArray( new  String[0]));
        msgHelper.setSubject(subject);

        if(appSettings.bccList() != null && !appSettings.bccList().isEmpty())
            msgHelper.setBcc(appSettings.bccList().toArray( new String[0]));

        if( appSettings.nibssLogo() != null && appSettings.nibssLogo().exists()) {
            Resource resource = new FileSystemResource(appSettings.nibssLogo());
            msgHelper.addInline(LOGO,resource);

        }

        if( resources != null && !resources.isEmpty()) {
            for (Resource resource : resources) {
                try {
                    String name = resource.getFilename() == null ? resource.getDescription() : resource.getFilename();
                    msgHelper.addAttachment(name, resource);
                } catch (Exception e) {
                    throw  new RuntimeException("could not attach resource",e);
                }
            }
        }

        return msgHelper;
    }
}

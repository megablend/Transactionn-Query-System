package com.nibss.tqs.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

import javax.jms.ConnectionFactory;

/**
 * Created by Emor on 7/9/16.
 */
@Configuration
@EnableJms
public class QueueConfig {

    public final static String USER_CREATION_QUEUE = "USER_CREATION_QUEUE";

    public final static String PASSWORD_RESET_QUEUE = "PASSWORD_RESET_QUEUE";

    public final static String WEEKLY_BILLING_QUEUE = "WEEKLY_BILLING_QUEUE";

    public final static String MERCHANT_REGISTRATION_QUEUE="MERCHANT_REGISTRATION_QUEUE";
    public static final String CL_ACCOUNT_PROFILING_QUEUE = "CL_ACCOUNT_PROFILING_QUEUE";
    public static final String CL_KEY_CHANGE = "CL_KEY_CHANGE" ;

    public static final String CL_NEW_CLIENT = "CL_NEW_CLIENT";
    public static final String CL_EXPIRING_ACCOUNTS = "CL_EXPIRING_ACCOUNTS" ;
    public static final String CL_ACCOUNT_STATUS_CHANGE = "CL_ACCOUNT_STATUS";
    public static  final String CL_EMAIL_NOTIFICATION = "CL_EMAIL_NOTIFICATION";

    @Autowired
    private ApplicationSettings applicationSettings;

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(applicationSettings.amqBrokerUrl());
    }


}

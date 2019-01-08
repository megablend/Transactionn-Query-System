package com.nibss.tqs.controllers;

import com.nibss.merchantpay.dto.MerchantRegistrationNotificationRequest;
import com.nibss.tqs.ajax.MerchantRegistrationDTO;
import com.nibss.tqs.config.QueueConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 4/10/2017.
 * Class used for merchant registration notification by Wale's system.
 */
@Controller
@RequestMapping("/api")
public class ApiController {


    @Autowired
    private JmsTemplate jmsTemplate;


    @RequestMapping(value = "/merchantregistration", method = RequestMethod.POST,consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> mapMerchantRegistration(@RequestBody MerchantRegistrationNotificationRequest request) {


        List<String> mCodes = request.getMerchant().stream().map(t -> t.getMerchantCode()).collect(Collectors.toList());

        MerchantRegistrationDTO dto = new MerchantRegistrationDTO(request.getHeader().getAggregatorCode(),
                request.getHeader().getAggregatorName(), mCodes);

        jmsTemplate.convertAndSend(QueueConfig.MERCHANT_REGISTRATION_QUEUE, dto);

        return ResponseEntity.accepted().body("Notification accepted");
    }
}

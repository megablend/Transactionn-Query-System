package com.nibss.tqs.config;

import com.nibss.tqs.ajax.MerchantRegistrationDTO;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.*;
import com.nibss.tqs.merchantpayment.MPMerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 4/10/2017.
 */
@Component
@Slf4j
public class ApiQueueListener {


    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private AggregatorRepository aggregatorRepository;

    @Autowired
    private MPMerchantRepository merchantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationSettingRepository organizationSettingRepository;


    @JmsListener(destination = QueueConfig.MERCHANT_REGISTRATION_QUEUE)
    @Transactional
    public void processMerchantRegistration(final MerchantRegistrationDTO dto) {

        if (dto.getAggregatorCode() == null || dto.getAggregatorCode().trim().isEmpty()) {
            log.warn("enroller code is null");
            return;
        }

        if (null == dto.getMerchantCodes() || dto.getMerchantCodes().isEmpty()) {
            log.warn("No merchantCodes sent");
            return;
        }

        List<Long> merchanIds = null;
        log.trace("About getting merchant IDs for merchant codes");
        try {
            merchanIds =  merchantRepository.findIdsByMerchantCode(dto.getMerchantCodes());
        } catch (Exception e) {
            log.error("could not get IDs for merchant codes",e);
            throw new RuntimeException(e);
        }

        if( null == merchanIds || merchanIds.isEmpty()) {
            log.trace("No merchant Ids gotten");
            return;
        }
        log.trace("No. of merchant Ids: {}", merchanIds.size());

        log.trace("Merchant IDs for processing: {}", dto.getMerchantCodes());
        log.trace("about getting details for code {}", dto.getAggregatorCode());

        boolean exception = false;

        Bank bank = null;
        Aggregator agg = null;

        try {
            bank = bankRepository.findByCode(dto.getAggregatorCode());
        } catch (Exception e) {
            exception = true;
            log.error("could not get bank by code", e);
        }

        if (null == bank) {
            try {
                agg = aggregatorRepository.findByCode(dto.getAggregatorCode());

                if( null == agg) { //possible this aggr is yet to be registered on platform. create new aggregator
                    agg = new Aggregator();
                    agg.setCode(dto.getAggregatorCode());
                    agg.setName(dto.getAggregatorName());
                    agg.setCreatedBy("api@nibss-plc.com.ng");
                    aggregatorRepository.save(agg);

                    OrganizationSetting setting = new OrganizationSetting();
                    setting.setNoOfAdmins(2);
                    setting.setNoOfOperators(5);
                    setting.setOrganization(agg);
                    setting.setDateModified(new Date());
                    setting.setModifiedBy("api@nibss-plc.com.ng");

                    organizationSettingRepository.save(setting);

                    Product mCashProd = productRepository.findByCode(Product.USSD_MERCHANT_PAYMENT);
                    productRepository.saveOrganizationProduct(agg.getId(), mCashProd.getId());
                }
            } catch (Exception e) {
                exception = true;
                log.error("could not get aggregator by code", e);
            }
        }

        if (exception) {
            sleepCurrent();
            jmsTemplate.convertAndSend(QueueConfig.MERCHANT_REGISTRATION_QUEUE, dto);
            return;
        }

        if( null != bank) {
            for( long i : merchanIds) {
                organizationRepository.deleteMcashMerchantByIdAndOrganizationType(i, OrganizationType.AGGREGATOR_INT);
                organizationRepository.deleteMcashMerchantByIdAndOrganizationType(i, OrganizationType.BANK_INT);

                organizationRepository.saveMcashMerchant(i, bank.getId());
            }
        } else if (null != agg) {
            for( long i : merchanIds) {
                organizationRepository.deleteMcashMerchantByIdAndOrganizationType(i, OrganizationType.AGGREGATOR_INT);
                organizationRepository.deleteMcashMerchantByIdAndOrganizationType(i, OrganizationType.BANK_INT);

                organizationRepository.saveMcashMerchant(i, agg.getId());
            }
        }

        log.trace("done saving merchant IDs to DB");
    }

    private void sleepCurrent() {
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}

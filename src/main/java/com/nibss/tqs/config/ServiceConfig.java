package com.nibss.tqs.config;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.billing.BillingNotification;
import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.core.repositories.UserRepository;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ebillspay.dto.EbillsBillingConfiguration;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.ebillspay.repositories.EbillsBillingConfigurationRepository;
import com.nibss.tqs.ebillspay.repositories.EbillsTransactionRepository;
import com.nibss.tqs.merchantpayment.MerchantPaymentTransactionRepository;
import com.nibss.tqs.ussd.dto.UssdTransaction;
import com.nibss.tqs.ussd.repositories.UssdTransactionRepository;
import com.nibss.tqs.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 8/19/2016.
 */
@Configuration
@EnableScheduling
@Slf4j
public class ServiceConfig {


    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private EbillsTransactionRepository ebillsTransactionRepository;

    @Autowired
    private EbillsBillingConfigurationRepository ebillsBillingConfigurationRepository;

    @Transactional
    @Scheduled(cron = "${user.disable_timing}")
    public void disableInactiveUsers() {
        LocalDateTime time = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN);
        Date date = Timestamp.valueOf(time);

        try {
            int count = userRepository.disableInactiveUsers(date);
            log.trace("No of users diabled: {}",count);
        } catch (Exception e) {
            log.error("could not deactivate inactive users",e);
        }
    }


    @Transactional
    @Scheduled(cron="${ebills_billing.fee_sharing_time}")
    public void calculateUnsharedEbillsTransactions() {
        try {
            List<EbillspayTransaction> transactions = ebillsTransactionRepository.findUnsharedTransactions();
            if( null == transactions || transactions.isEmpty()) {
                return;
            }

            log.trace("No of transactions for sharing: {}", transactions.size());
            Map<Biller,List<EbillspayTransaction>> byBiller = transactions.stream().collect(Collectors.groupingBy( t -> t.getBaseTransaction().getBiller()));

            byBiller.forEach( (k,v) -> {
                EbillsBillingConfiguration config = ebillsBillingConfigurationRepository.findForBiller(k.getId());
                if( null == config) {
                    log.trace("config is null for biller {}",k.getName());
                } else {
                    v.forEach( t -> {
                        BigDecimal aggrShare = Utility.getShare(config.getAggregatorShare(),t.getBaseTransaction().getTransactionFee(), config.isPercentage());
                        t.setAggregatorShare(aggrShare.setScale(2,BillingProvider.ROUNDING_MODE));

                        BigDecimal billerBankShare = Utility.getShare( config.getBillerBankShare(),t.getBaseTransaction().getTransactionFee(),config.isPercentage());
                        t.setBillerBankShare(billerBankShare.setScale(2,BillingProvider.ROUNDING_MODE));

                        BigDecimal colBankShare = Utility.getShare( config.getCollectingBankShare(),t.getBaseTransaction().getTransactionFee(),config.isPercentage());
                        t.setCollectingBankShare(colBankShare.setScale(2,BillingProvider.ROUNDING_MODE));

                        BigDecimal nibssShare = Utility.getShare( config.getNibssShare(),t.getBaseTransaction().getTransactionFee(),config.isPercentage());
                        t.setNibssShare(nibssShare.setScale(2,BillingProvider.ROUNDING_MODE));

                        t.setShared(true);
                    }

                    );
                }

            });

            ebillsTransactionRepository.save(transactions);
            log.trace("done sharing transaction fees");

        } catch (Exception e) {
            log.error("could not calculate unshared transactions",e);
        }
    }

}

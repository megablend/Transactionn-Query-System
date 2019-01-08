package com.nibss.tqs.config;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.billing.BillingNotification;
import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
import com.nibss.tqs.centralpay.repositories.AccountTransactionRepository;
import com.nibss.tqs.ebillspay.dto.BillingCycle;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.ebillspay.repositories.EbillsTransactionRepository;
import com.nibss.tqs.merchantpayment.MerchantPaymentTransactionRepository;
import com.nibss.tqs.ussd.dto.UssdTransaction;
import com.nibss.tqs.ussd.repositories.UssdTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by Emor on 9/26/16.
 */
@Configuration
@EnableScheduling
@Slf4j
public class BillingServiceConfig {

    @Autowired
    @Qualifier("merchantPayBillingProvider")
    private BillingProvider merchantBillingProvider;

    @Autowired
    @Qualifier("ussdBillPaymentBillingProvider")
    private BillingProvider ussdBillingProvider;

    @Autowired
    @Qualifier("ebillsTransactionTimeBillingProvider")
    private BillingProvider ebillsTransactionTimeProvider;

    @Autowired
    @Qualifier("customBillerBillingProvider")
    private BillingProvider ebillsCustomBillingProvider;

    @Autowired
    @Qualifier("cpayBillingProvider")
    private BillingProvider cpayBillingProvider;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private EbillsTransactionRepository ebillsTransactionRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private MerchantPaymentTransactionRepository merchantPaymentTransactionRepository;

    @Autowired
    private UssdTransactionRepository ussdTransactionRepository;

    @Scheduled(cron = "${merchantpay.billing_time}")
    @Transactional
    public void doMerchantPayBilling() {
        try {
            List<DebitTransaction> transactions = merchantPaymentTransactionRepository.getTransactionsForWeeklyBilling();
            if (null == transactions || transactions.isEmpty()) {
                return;
            }
            Path billingFilePath = merchantBillingProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.WEEKLY);
            if (null != billingFilePath) {
                String fullPathName = billingFilePath.toAbsolutePath().toString();
                BillingNotification notification = new BillingNotification(fullPathName, "USSD Merchant Payment", "Weekly billing");
                transactions.forEach(t -> t.setBilled(true));
                merchantPaymentTransactionRepository.save(transactions);
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                log.trace("generating report for parties");
                merchantBillingProvider.generatePartyReports(transactions, BillingProvider.BillingPeriod.WEEKLY);
                log.trace("done generating reports");
            }

        } catch (IOException e) {
            log.error("could not generate billing for merchant payment", e);
        }

        merchantBillingProvider.cleanUp();
    }

    @Scheduled(cron = "${ussd_billing.billing_time}")
    @Transactional
    public void doUssdBillPaymentBilling() {
        try {
            List<UssdTransaction> transactions = ussdTransactionRepository.getTransactionsForWeeklyBilling();
            if (null == transactions || transactions.isEmpty()) {
                return;
            }
            Path billingFilePath = ussdBillingProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.WEEKLY);
            if (null != billingFilePath) {
                String fullPathName = billingFilePath.toAbsolutePath().toString();
                BillingNotification notification = new BillingNotification(fullPathName, "mCASH", "Weekly billing");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);
                transactions.stream().forEach(t -> t.setBilled(true));
                ussdTransactionRepository.save(transactions);

                ussdBillingProvider.generatePartyReports(transactions, BillingProvider.BillingPeriod.WEEKLY);
            }

        } catch (IOException e) {
            log.error("could not generate billing for ussd bill payment", e);
        }

        ussdBillingProvider.cleanUp();
    }

    @Scheduled(cron = "${ebills_billing.transaction_time_billing}")
    @Transactional
    public void doEbillsTransactionTimeWeeklyBilling() {

        try {
            List<EbillspayTransaction> transactions = ebillsTransactionRepository.getWeeklyTransactionTimeTransactions();
            if (null == transactions || transactions.isEmpty()) {
                return;
            }
            Path billingPath = ebillsTransactionTimeProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.WEEKLY);
            if (null != billingPath) {
                BillingNotification notification = new BillingNotification(
                        billingPath.toAbsolutePath().toString(), "e-BillsPay Transaction Time Weekly Billing", "Weekly billing");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                transactions.forEach(t -> t.setBilled(true));
                ebillsTransactionRepository.save(transactions);

                ebillsTransactionTimeProvider.generatePartyReports(transactions, BillingProvider.BillingPeriod.WEEKLY);

            }

        } catch (Exception e) {
            log.error("could not do ebills transaction time billing", e);
        }

        ebillsTransactionTimeProvider.cleanUp();
    }

    @Scheduled(cron = "${ebills_billing.custom_weekly}")
    @Transactional
    public void doCustomBillerWeeklyBilling() {
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);

        try {
            List<EbillspayTransaction> txns = ebillsTransactionRepository.getCustomBillerTransactions(startDateTime, endDateTime, BillingCycle.WEEKLY);
            if (null == txns || txns.isEmpty()) {
                return;
            }

            Path billingPath = ebillsCustomBillingProvider.getBillingZipFile(txns, BillingProvider.BillingPeriod.WEEKLY);
            if (null != billingPath) {

                BillingNotification notification = new BillingNotification(
                        billingPath.toAbsolutePath().toString(), "e-BillsPay Custom  Biller Weekly Billing", "Weekly billing");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                txns.forEach(t -> t.setBilled(true));
                ebillsTransactionRepository.save(txns);

                ebillsCustomBillingProvider.generatePartyReports(txns, BillingProvider.BillingPeriod.WEEKLY);
            }
        } catch (Exception e) {
            log.error("could not do custom ebills weekly billing", e);
        }

        ebillsCustomBillingProvider.cleanUp();
    }

    @Scheduled(cron = "${ebills_billing.custom_monthly}")
    @Transactional
    public void doCustomBillerMonthlyBilling() {

        LocalDate start = LocalDate.now().minusMonths(1);

        LocalDate startDate = LocalDate.of(start.getYear(), start.getMonthValue(), 1);
        LocalDate endDate = LocalDate.of(start.getYear(), start.getMonthValue(), start.lengthOfMonth());

        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);

        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);

        try {
            List<EbillspayTransaction> txns = ebillsTransactionRepository.getCustomBillerTransactions(startDateTime, endDateTime, BillingCycle.MONTHLY);
            if (null == txns || txns.isEmpty()) {
                return;
            }

            Path billingPath = ebillsCustomBillingProvider.getBillingZipFile(txns, BillingProvider.BillingPeriod.MONTHLY);
            if (null != billingPath) {
                BillingNotification notification = new BillingNotification(
                        billingPath.toAbsolutePath().toString(), "e-BillsPay Custom  Biller Monthly Billing", "Monthly billing");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                txns.forEach(t -> t.setBilled(true));
                ebillsTransactionRepository.save(txns);
                ebillsCustomBillingProvider.generatePartyReports(txns, BillingProvider.BillingPeriod.MONTHLY);
            }
        } catch (Exception e) {
            log.error("could not do custom ebills monthly billing", e);
        }

        ebillsCustomBillingProvider.cleanUp();
    }

    @Scheduled(cron = "${cpay_billing.billing_time}")
    @Transactional
    public void doCpyAccountBilling() {

        try {
            List<AccountTransaction> txns = accountTransactionRepository.getTransactionsWeeklyBilling();
            if (null == txns || txns.isEmpty()) {
                return;
            }
            Path billingPath = cpayBillingProvider.getBillingZipFile(txns, BillingProvider.BillingPeriod.WEEKLY);
            if (billingPath != null) {
                BillingNotification notification = new BillingNotification(billingPath.toAbsolutePath().toString(), "CentralPay Account Billing", "Weekly billing");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                txns.forEach(t -> t.setBilled(true));
                accountTransactionRepository.save(txns);
                cpayBillingProvider.generatePartyReports(txns, BillingProvider.BillingPeriod.WEEKLY);
            }

        } catch (Exception e) {
            log.error("could not do cpay account billing", e);
        }

    }

    @Scheduled(cron = "${cpay_billing.backlogs}")
    @Transactional
    public void doCentralPayBacklogsBilling() {
        try {
            List<AccountTransaction> backlogs = accountTransactionRepository.getTransactionsForBacklogs();
            if (null == backlogs || backlogs.isEmpty()) {
                return;
            }
            Path billingPath = cpayBillingProvider.getBillingZipFile(backlogs, BillingProvider.BillingPeriod.BACKLOGS);
            if (null != billingPath) {
                BillingNotification not = new BillingNotification(billingPath.toAbsolutePath().toString(), "CentralPay Account Billing", "backlogs");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, not);

                backlogs.forEach(t -> t.setBilled(true));
                accountTransactionRepository.save(backlogs);
                cpayBillingProvider.generatePartyReports(backlogs, BillingProvider.BillingPeriod.BACKLOGS);
            }
        } catch (Exception e) {
            log.error("could not do cpay account backlogs", e);
        }
    }

    @Scheduled(cron = "${ussd_billing.backlogs}")
    @Transactional
    public void doUssdBillPaymentBacklogs() {
        try {
            List<UssdTransaction> transactions = ussdTransactionRepository.getBacklogTransactions();
            if (null == transactions || transactions.isEmpty()) {
                return;
            }
            Path billingFilePath = ussdBillingProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.BACKLOGS);
            if (null != billingFilePath) {
                String fullPathName = billingFilePath.toAbsolutePath().toString();
                BillingNotification notification = new BillingNotification(fullPathName, "mCASH", "backlogs");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);
                transactions.stream().forEach(t -> t.setBilled(true));
                ussdTransactionRepository.save(transactions);

                ussdBillingProvider.generatePartyReports(transactions, BillingProvider.BillingPeriod.BACKLOGS);
            }

        } catch (IOException e) {
            log.error("could not generate billing for merchant payment", e);
        }
    }

    @Scheduled(cron = "${merchantpay.backlogs}")
    @Transactional
    public void doMerchantPayBacklogs() {
        try {
            List<DebitTransaction> transactions = merchantPaymentTransactionRepository.getBacklogTransactions();
            if (null == transactions || transactions.isEmpty()) {
                return;
            }
            Path billingFilePath = merchantBillingProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.BACKLOGS);
            if (null != billingFilePath) {
                String fullPathName = billingFilePath.toAbsolutePath().toString();
                BillingNotification notification = new BillingNotification(fullPathName, "USSD Merchant Payment", "backlogs");
                transactions.forEach(t -> t.setBilled(true));
                merchantPaymentTransactionRepository.save(transactions);
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                log.trace("generating report for parties");
                merchantBillingProvider.generatePartyReports(transactions, BillingProvider.BillingPeriod.BACKLOGS);
                log.trace("done generating reports");
            }
        } catch (IOException e) {
            log.error("could not generate billing for merchant payment backlogs", e);
        }
        merchantBillingProvider.cleanUp();
    }

    @Scheduled(cron = "${ebills_billing.transaction_time_backlogs}")
    @Transactional
    public void doEbillsTransactionTimeBacklogs() {
        try {
            List<EbillspayTransaction> transactions = ebillsTransactionRepository.getBacklogsForTransactionTimeTaken();
            if (null == transactions || transactions.isEmpty()) {
                return;
            }
            Path billingPath = ebillsTransactionTimeProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.BACKLOGS);
            if (null != billingPath) {
                BillingNotification notification = new BillingNotification(
                        billingPath.toAbsolutePath().toString(), "e-BillsPay Transaction Time Weekly Billing", "backlogs");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                transactions.forEach(t -> t.setBilled(true));
                ebillsTransactionRepository.save(transactions);

                ebillsTransactionTimeProvider.generatePartyReports(transactions, BillingProvider.BillingPeriod.BACKLOGS);

            }

        } catch (Exception e) {
            log.error("could not do ebills transaction time billing", e);
        }

        ebillsTransactionTimeProvider.cleanUp();
    }

    @Scheduled(cron = "${ebills_billing.transaction_nontime_backlogs}")
    @Transactional
    public void doEbillsNonTransactionTimeBacklogs() {
        try {
            List<EbillspayTransaction> transactions = ebillsTransactionRepository.getBacklogsForNonTransactionTimeTaken();
            if (null == transactions || transactions.isEmpty()) {
                return;
            }
            Path billingPath = ebillsCustomBillingProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.BACKLOGS);
            if (null != billingPath) {
                BillingNotification notification = new BillingNotification(
                        billingPath.toAbsolutePath().toString(), "e-BillsPay Non-Transaction Time Weekly Billing", "backlogs");
                sendToNotificationQueue(QueueConfig.WEEKLY_BILLING_QUEUE, notification);

                transactions.forEach(t -> t.setBilled(true));
                ebillsTransactionRepository.save(transactions);

                ebillsCustomBillingProvider.generatePartyReports(transactions, BillingProvider.BillingPeriod.BACKLOGS);

            }

        } catch (Exception e) {
            log.error("could not do ebills transaction time billing", e);
        }

        ebillsCustomBillingProvider.cleanUp();
    }

    private void sendToNotificationQueue(String queueName, BillingNotification notification) {
        jmsTemplate.convertAndSend(queueName, notification);
        log.trace("sent to notification queue");
    }
}

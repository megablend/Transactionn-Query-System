package com.nibss.tqs.controllers;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountBalance;
import com.nibss.corporatelounge.dto.PaymentMode;
import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.ajax.AjaxResponse;
import com.nibss.tqs.billing.BillingNotification;
import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
import com.nibss.tqs.centralpay.repositories.AccountTransactionRepository;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.config.QueueConfig;
import com.nibss.tqs.corporatelounge.repositories.AccountBalanceRepository;
import com.nibss.tqs.corporatelounge.service.AccountService;
import com.nibss.tqs.ebillspay.dto.BillingCycle;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.ebillspay.repositories.EbillsTransactionRepository;
import com.nibss.tqs.merchantpayment.MerchantPaymentTransactionRepository;
import com.nibss.tqs.ussd.dto.UssdTransaction;
import com.nibss.tqs.ussd.repositories.UssdTransactionRepository;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Emor on 9/12/16.
 */
@Controller
@RequestMapping("/billing")
@Slf4j
@Transactional
public class BillingController {


    @Autowired
    @Qualifier("merchantPayBillingProvider")
    private BillingProvider merchantPayBillingProvider;

    @Autowired
    @Qualifier("ussdBillPaymentBillingProvider")
    private  BillingProvider ussdBillingProvider;
    
    @Autowired
    @Qualifier("cpayBillingProvider")
    private BillingProvider cpayBillingProvider;
    
    
    @Autowired
    @Qualifier("ebillsTransactionTimeBillingProvider")
    private BillingProvider ebillsTrxnTimeBillingProvider;
    
    @Autowired
    @Qualifier("customBillerBillingProvider")
    private BillingProvider ebillsCustomBillingProvider;

    @Autowired
    @Qualifier("clBillingPerTransactionProvider")
    private BillingProvider corpLoungePerTransactionProvider;

    @Autowired
    @Qualifier("clAnnualBillingProvider")
    private BillingProvider corpLoungeAnnualProvider;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("tqsExecutorService")
    private ExecutorService executorService;

    @Autowired
    private UssdTransactionRepository ussdTransactionRepository;

    @Autowired
    private MerchantPaymentTransactionRepository merchantPayRepository;
    
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;
    
    @Autowired
    private EbillsTransactionRepository ebillsTransactionRepository;


    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Autowired
    private ApplicationSettings appSettings;


    @RequestMapping(value = "",method = RequestMethod.GET)
    public String index(Model model) {

        return "billing/index";
    }


    @RequestMapping(value = "",method = RequestMethod.POST)
    @Transactional
    public @ResponseBody AjaxResponse generateReport(Authentication auth,
                                                     @RequestParam("product") String product,
                                                     @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")Date startDate,
                                                     @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                                     @RequestParam("generateReports") Boolean generateReports) {


        if( null == auth.getPrincipal())
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED,null);


        LocalDateTime startDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

        log.trace("Start Date for billing: {}",startDateTime);
        log.trace("End Date for billing: {}",endDateTime);
        log.trace("Product for billing: {}",product.toLowerCase());

        Runnable r;
        switch (product.toLowerCase()) {
            case "ussd":
                r = doUssdBilling(startDateTime,endDateTime,generateReports);
                break;
            case "mpay":
                r = doMerchantPayBilling(startDateTime,endDateTime,generateReports);
                break;
            case "cpay":
                r = doCpayAccountBilling(startDateTime, endDateTime, generateReports);
                break;
            case "ebills":
                r = doEbillsTransactionTimeBilling(startDateTime, endDateTime, generateReports);
                break;
            case "ebills_custom":
                r = doEbillsCustomTimeBilling(startDateTime, endDateTime, generateReports);
                break;
            default:
                return OrganizationController.buildFailedResponse("A valid product was not specified", null);
            case "cl_annual":
                r = doCorporateLoungeAnnualBilling(startDateTime, endDateTime, generateReports);
                break;
            case "cl_transaction":
                r = doCorporateLoungePerTransactionBilling(startDateTime,endDateTime,generateReports);
                break;
                
            // TODO: 9/12/16 add other entries here
        }

        if( null != r)
            executorService.execute(r);

        return OrganizationController.buildSuccessResponse("Your request has been queued. The switching/billing team should get a mail with the generated attachment shortly, if " +
                " transactions exists for the specified period");
    }


    private Runnable doMerchantPayBilling(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean genReports) {

      Runnable r = () -> {
          try {

              List<DebitTransaction> transactions = merchantPayRepository.getTransactionsForBillingPeriod(startDateTime, endDateTime);
              if( null != transactions && !transactions.isEmpty()) {
                  Path billingPath = merchantPayBillingProvider.getBillingZipFile(transactions, BillingProvider.BillingPeriod.PERIOD);
                  if( null != billingPath) {
                      String periodInfo = String.format(" for the period %s to %s", getFormattedDate(startDateTime), getFormattedDate(endDateTime));
                       BillingNotification notification = new BillingNotification(
                       billingPath.toAbsolutePath().toString(), "mCASH Billing",periodInfo);
                       sendToQueue(notification);
                  }
                  if( genReports)
                      merchantPayBillingProvider.generatePartyReports(transactions,BillingProvider.BillingPeriod.PERIOD);
              } else
                  log.trace("no merchant pay transactions within period");
          } catch(Exception e) {

              log.error("could not generate merchant pay billing report",e);
          }
        };

        return  r;
    }


    private Runnable doCorporateLoungeAnnualBilling(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean genReports) {
        Runnable r = () -> {

            Date startDate = Timestamp.valueOf(startDateTime);
            Date endDate = Timestamp.valueOf(endDateTime);
            try {
                List<Account> accounts = accountService.findForBilling(startDate, endDate, PaymentMode.ANNUAL);
                if( null == accounts || accounts.isEmpty()) {
                    log.info("no CL. Annual Accounts for billing for period {} to {}", startDateTime, endDateTime);
                    return;
                }
                Path billingPath = corpLoungeAnnualProvider.getBillingZipFile(accounts, BillingProvider.BillingPeriod.PERIOD);
                if( null != billingPath) {
                    String periodInfo = String.format(" for the period %s to %s", getFormattedDate(startDateTime), getFormattedDate(endDateTime));
                    BillingNotification notification = new BillingNotification(
                            billingPath.toAbsolutePath().toString(), "Corporate Lounge Annual-subscription Billing",periodInfo);
                    sendToQueue(notification);
                }
                if(genReports)
                    corpLoungeAnnualProvider.generatePartyReports(accounts, BillingProvider.BillingPeriod.PERIOD);
            } catch (Exception e) {
                log.error("could not generate corp. lounge annual billing report",e);
            }
        };

        return r;
    }

    private Runnable doCorporateLoungePerTransactionBilling(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean genReports) {
        Runnable r = () -> {

            Date startDate = Timestamp.valueOf(startDateTime);
            Date endDate = Timestamp.valueOf(endDateTime);
            try {
                List<AccountBalance> balances = accountBalanceRepository.getForDurationAndPaymentMode(startDate,endDate,PaymentMode.TRANSACTION);
                if( null == balances || balances.isEmpty()) {
                    log.info("no CL. Per-Transaction Accounts for billing for period {} to {}", startDateTime, endDateTime);
                    return;
                }
                Path billingPath = corpLoungePerTransactionProvider.getBillingZipFile(balances, BillingProvider.BillingPeriod.PERIOD);
                if( null != billingPath) {
                    String periodInfo = String.format(" for the period %s to %s", getFormattedDate(startDateTime), getFormattedDate(endDateTime));
                    BillingNotification notification = new BillingNotification(
                            billingPath.toAbsolutePath().toString(), "Corporate Lounge Per-Transaction subscription Billing",periodInfo);
                    sendToQueue(notification);
                }
                if(genReports)
                    corpLoungePerTransactionProvider.generatePartyReports(balances, BillingProvider.BillingPeriod.PERIOD);
            } catch (Exception e) {
                log.error("could not generate corp. lounge annual billing report",e);
            }
        };

        return r;
    }

    public Runnable doCpayAccountBilling(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean genReports) {
        Runnable r = () -> {
             try {
                 List<AccountTransaction> transactions = accountTransactionRepository.getTransactionsForBillingPeriod(startDateTime, endDateTime);
                 if( null == transactions || transactions.isEmpty()) {
                     log.trace("no cpay transactions within period");
                     return;
                 }

                 Path billingPath = cpayBillingProvider.getBillingZipFile(transactions,BillingProvider.BillingPeriod.PERIOD);
                 if( null != billingPath) {
                      String periodInfo = String.format(" for the period %s to %s", getFormattedDate(startDateTime), getFormattedDate(endDateTime));
                       BillingNotification notification = new BillingNotification(
                       billingPath.toAbsolutePath().toString(), "CentralPay Account Billing",periodInfo);
                       sendToQueue(notification);
                 }
                 if( genReports)
                     cpayBillingProvider.generatePartyReports(transactions,BillingProvider.BillingPeriod.PERIOD);
             } catch(Exception e) {
                 log.error("could not generate cpay acct payment report", e);
             }
        };
        
        return r;
    }

    public Runnable doUssdBilling(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean genReports) {
        Runnable r = () -> {

            try {
                List<UssdTransaction> transactions = ussdTransactionRepository.getTransactionsForPeriod(startDateTime,endDateTime);
                if( null != transactions && !transactions.isEmpty()) {
                    Path ussdPath = ussdBillingProvider.getBillingZipFile(transactions,BillingProvider.BillingPeriod.PERIOD);
                    if(null != ussdPath) {
                       String periodInfo = String.format(" for the period %s to %s", getFormattedDate(startDateTime), getFormattedDate(endDateTime));
                       BillingNotification notification = new BillingNotification(
                       ussdPath.toAbsolutePath().toString(), "USSD Bill Payment Billing",periodInfo);
                       sendToQueue(notification);
                    }
                    if(genReports)
                        ussdBillingProvider.generatePartyReports(transactions,BillingProvider.BillingPeriod.PERIOD);

                } else
                    log.trace("no ussd transactions within period");
            } catch(Exception e) {
                log.error("could not generate  ussd billing report",e);
            }
        };
        return r;
    }
    
    public Runnable doEbillsTransactionTimeBilling(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean genReports) {
        Runnable r = () -> {

            try {
                log.trace("about generating ebills billing for date range");
                List<EbillspayTransaction> transactions = ebillsTransactionRepository.getTransactionTimeTransactions(startDateTime, endDateTime);
                if( null != transactions && !transactions.isEmpty()) {
                    log.trace("total txns gotten for period: {}", transactions.size());
                    BigDecimal totalFee = transactions.stream().map( t -> t.getBaseTransaction().getTransactionFee())
                            .reduce( (a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
                    log.trace("total fee for period: {}", totalFee);
                    Path billingPath = ebillsTrxnTimeBillingProvider.getBillingZipFile(transactions,BillingProvider.BillingPeriod.PERIOD);
                    if(null != billingPath) {
                       String periodInfo = String.format(" for the period %s to %s", getFormattedDate(startDateTime), getFormattedDate(endDateTime));
                       BillingNotification notification = new BillingNotification(
                       billingPath.toAbsolutePath().toString(), "eBillsPay Transaction Time Billing",periodInfo);
                       sendToQueue(notification);
                    }
                    if(genReports)
                        ebillsTrxnTimeBillingProvider.generatePartyReports(transactions,BillingProvider.BillingPeriod.PERIOD);
                    
                    log.trace("done generating ebills billing for date range");

                } else
                    log.trace("no ebills transactions within period");
            } catch(Exception e) {
                log.error("could not generate  ebills trxn time billing report",e);
            }
        };
        return r;
    }
    
    
    public Runnable doEbillsCustomTimeBilling(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean genReports) {
        Runnable r = () -> {

            try {
                List<EbillspayTransaction> transactions = new ArrayList<>();
                List<EbillspayTransaction> transactionsMonthly = ebillsTransactionRepository.getCustomBillerTransactions(startDateTime, endDateTime, BillingCycle.MONTHLY);
                List<EbillspayTransaction> transactionsWeekly = ebillsTransactionRepository.getCustomBillerTransactions(startDateTime, endDateTime, BillingCycle.WEEKLY);
                
                if( null != transactionsMonthly)
                    transactions.addAll(transactionsMonthly);
                if( null != transactionsWeekly)
                    transactions.addAll(transactionsWeekly);
                
                if( null != transactions && !transactions.isEmpty()) {
                    Path billingPath = ebillsCustomBillingProvider.getBillingZipFile(transactions,BillingProvider.BillingPeriod.PERIOD);
                    if(null != billingPath) {
                       String periodInfo = String.format(" for the period %s to %s", getFormattedDate(startDateTime), getFormattedDate(endDateTime));
                       BillingNotification notification = new BillingNotification(
                       billingPath.toAbsolutePath().toString(), "eBillsPay Non-transaction time Billing",periodInfo);
                       sendToQueue(notification);
                    }
                    if(genReports)
                        ebillsCustomBillingProvider.generatePartyReports(transactions,BillingProvider.BillingPeriod.PERIOD);

                } else
                    log.trace("no ebills transactions within period");
            } catch(Exception e) {
                log.error("could not generate  ebills custom billing report",e);
            }
        };
        return r;
    }
    
    private void sendToQueue(final BillingNotification notification) {
        jmsTemplate.convertAndSend(QueueConfig.WEEKLY_BILLING_QUEUE, notification);
    }

    private String getFormattedDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss"));
    }
}

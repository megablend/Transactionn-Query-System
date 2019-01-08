package com.nibss.tqs.ussd.merchantpay.billing;

import com.nibss.merchantpay.entity.*;
import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.entities.Aggregator;
import com.nibss.tqs.core.repositories.BankAccountRepository;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.core.repositories.MerchantPaymentSharingConfigRepository;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.merchantpayment.MerchantPaymentTransactionRepository;
import com.nibss.tqs.merchantpayment.TelcoRepository;
import com.nibss.tqs.util.BillingHelper;
import com.nibss.tqs.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 8/18/2016.
 */
@Component("merchantPayBillingProvider")
@Slf4j
@Transactional
public class MerchantPayBillingProvider implements BillingProvider {

    @Autowired
    private MerchantPaymentSharingConfigRepository merchantPaymentSharingConfigRepository;

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TelcoRepository telcoRepository;

    @Autowired
    private MerchantPaymentTransactionRepository merchantPaymentTransactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BillingHelper billingHelper;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private MerchantPaymentReportHelper reportHelper;

    Map<Long, String> merchantIntroducerMap = new HashMap<>();

    Map<String, BankAccount> partyAccountMap = new HashMap<>();

    private static String[] DETAIL_HEADER = {"DEBIT SESSION ID", "CREDIT SESSION ID","MERCHANT NAME", "MERCHANT CODE", "REFERENCE CODE",
            "PAYMENT REF", "TELCO", "USSD AGGREGATOR","PAYER BANK", "MERCHANT BANK", "MERCHANT INTRODUCER",
        "AMOUNT", "TRANSACTION FEE", "NIBSS SHARE", "USSD AGGR. SHARE", "TELCO SHARE", "SCHEME ROYALTY SHARE",
        "PAYER BANK SHARE", "MERCHANT BANK SHARE", "MERCHANT INTRODUCER SHARE"};

    @Override
    @Transactional
    public Path getBillingZipFile(List<? extends Serializable> transactions, BillingPeriod billingPeriod) throws RuntimeException, IOException {

        //re-initialize map for every run
        partyAccountMap.clear();

        List<DebitTransaction> debitTransactions = transactions.stream().map(t -> (DebitTransaction) t).collect(Collectors.toList());

        MerchantPaymentSharingConfig config = null;
        log.trace("No. of transactions for billing: {}", debitTransactions.size());
        try {
            List<MerchantPaymentSharingConfig> items = merchantPaymentSharingConfigRepository.findAll();
            if (null != items && !items.isEmpty()) {
                config = items.get(0);
            }
        } catch (Exception e) {
            log.error("could not get sharing config", e);
        }

        if (null == config) {
            log.trace("no config has been maintained");
            return null;
        }

        StopWatch timer = new StopWatch();
        timer.start();
        Map<Institution, List<DebitTransaction>> trxnsByBank = debitTransactions.stream().collect(Collectors.groupingBy(t -> t.getInstitution()));
        timer.stop();

        Map<String, BigDecimal> smartDetMap = new HashMap<>();

        log.trace("About building smart det file");

      /*  trxnsByBank.forEach( (k,v) -> {
            for( DebitTransaction t : v) {
                BigDecimal debitFee = t.getFee() == null ? BigDecimal.ZERO : t.getFee();
                smartDetMap.put(k.getBankCode(), debitFee);
                CreditTransaction ct = t.getCreditTransaction();

                if( !Utility.isEmptyBigDecimal(ct.getFee())) {
                    BigDecimal creditFee = ct.getFee();
                    String creditBankCode = ct.getInstitution().getBankCode();
                    if( smartDetMap.containsKey(creditBankCode))
                        smartDetMap.put(creditBankCode, smartDetMap.get(creditBankCode).add(creditFee));
                    else
                        smartDetMap.put(creditBankCode, creditFee);
                }
            }
        });*/

        trxnsByBank.forEach((k, v) -> smartDetMap.put(k.getBankCode(), v.stream().map(t -> {
            BigDecimal fee = t.getFee().abs();
            if( t.getCreditTransaction() != null) {
                if(t.getCreditTransaction().getFee() != null)
                    fee = fee.add(t.getCreditTransaction().getFee().abs());
            }
            return  fee;
        }).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO)));

        String smardetContent = billingHelper.getSmartDetFile(smartDetMap, appSettings.merchantPaySmarDetCode());
        log.trace("done building smartdet file");

        Map<BankAccount, BigDecimal> sharedSums = new HashMap<>();

        log.trace("about sharing transaction fee for transactions");
        timer = new StopWatch();
        timer.start();
        MerchantPaymentSharingConfig theConfig = config;
        debitTransactions.forEach(t -> shareTransactionFee(t, sharedSums, theConfig));
        timer.stop();
        log.trace("done sharing transaction fee for transactions. Time taken in ms: {}", timer.getTotalTimeMillis());

        String nibssPayFile = billingHelper.getNIBSSPayPaymentFile(sharedSums, appSettings.merchantPaymentNarration(), appSettings.merchantPaymentPayerName());

        log.trace("about generating zip file");
        timer = new StopWatch();
        timer.start();
        billingHelper.createPaymentProductFolder(Product.USSD_MERCHANT_PAYMENT);

        DateFormat dtFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH);

        String strDate = dtFormat.format(new Date());
        String zipFileName = String.format("MerchantPayBilling_period_%s_%s.zip", strDate,billingPeriod);

        Path merchantPaymentFilePath = Paths.get(appSettings.billingPaymentFolder(), Product.USSD_MERCHANT_PAYMENT, zipFileName);

        Map<String, String> filesMap = new HashMap<>();


        String smarDetFileName = String.format("MerchantPaySmartDet_%s.txt", strDate);
        String nibssPaymentFileName = String.format("MerchantPayNIBSSPaymentFile_%s.txt", strDate);
        String transactionDetailFile = String.format("TransactionDetails_%s.csv", strDate);

        filesMap.put(smarDetFileName, smardetContent);
        filesMap.put(nibssPaymentFileName, nibssPayFile);
        String detailsContent = buildTransactionDetailReport(debitTransactions, theConfig);

        filesMap.put(transactionDetailFile, detailsContent);

        //write zip file to disc
        billingHelper.writePaymentZipFile(filesMap, merchantPaymentFilePath);
        timer.stop();
        log.trace("done generating zip payment file. Time taken in ms: {}", timer.getTotalTimeMillis());
        merchantIntroducerMap.clear();

        return merchantPaymentFilePath;
    }

    @Override
    @Transactional
    public void generatePartyReports(List<? extends Serializable> transactions, BillingPeriod billingPeriod) {
        List<DebitTransaction> debitTransactions = transactions.stream().map(t -> (DebitTransaction) t).collect(Collectors.toList());
        try {
            reportHelper.generateReports(debitTransactions, merchantPaymentSharingConfigRepository.findAll().get(0));
        } catch (Exception e) {
            log.error("could not generate report", e);
        }
    }

    @Override
    public void cleanUp() {
        if (null != merchantIntroducerMap) {
            merchantIntroducerMap.clear();
        }
    }

    private void shareTransactionFee(final DebitTransaction dt, final Map<BankAccount, BigDecimal> map, MerchantPaymentSharingConfig config) {

        /*computations are done based on the assumption that config values are maintained in fractions, not whole numbers. ie. 0.05, not 5% */
        BigDecimal payerBankShare;
        BigDecimal merchantBankShare;
        BigDecimal ussdAggregatorShare;
        BigDecimal telcoShare;


        BigDecimal creditFee = dt.getCreditTransaction() != null && dt.getCreditTransaction().getFee() != null ?
                dt.getCreditTransaction().getFee() : BigDecimal.ZERO;

        BigDecimal totalFee = dt.getFee().add(creditFee);

        //if acct detail for any of the parties below is null, what happens?
        if (null != config.getPayerBankShare() && config.getPayerBankShare().compareTo(BigDecimal.ZERO) > 0) {
//            log.trace("about adding payer bank share");
            Bank payerBank = bankRepository.findByCode(dt.getInstitution().getBankCode());

            if (null != payerBank) {
                BankAccount payerBankAcct = getForParty(payerBank.getCbnBankCode());
                if( null == payerBankAcct) {
                    try {
                        payerBankAcct = bankAccountRepository.findByOrganizationAndProductCode(payerBank.getId(), Product.USSD_MERCHANT_PAYMENT);
                    } catch (Exception e) {
                        log.error("could not get acct for payer bank {}", payerBank, e);
                    }
                }

                if (null != payerBankAcct) {
                    partyAccountMap.put(payerBank.getCbnBankCode(), payerBankAcct);
                    payerBankShare = config.getPayerBankShare().multiply(totalFee).setScale(2, ROUNDING_MODE);
                    if (map.containsKey(payerBankAcct)) {
                        map.put(payerBankAcct, map.get(payerBankAcct).add(payerBankShare));
                    } else {
                        map.put(payerBankAcct, payerBankShare);
                    }
                } else
                    log.warn("No bank acct specified for payer bank {} for product {}", payerBank.getName(), Product.USSD_MERCHANT_PAYMENT);
            }

//            log.trace("done adding payer bank share");
        }

        if (null != config.getMerchantBankShare() && config.getMerchantBankShare().compareTo(BigDecimal.ZERO) > 0) {
//            log.trace("about adding merchant bank share");

            Bank merchantBank = bankRepository.findByCode(dt.getCreditTransaction().getInstitution().getBankCode());
            if (null != merchantBank) {
                BankAccount merchantAcct = getForParty(merchantBank.getCbnBankCode());

               if( merchantAcct == null) {
                   try {
                       merchantAcct = bankAccountRepository.findByOrganizationAndProductCode(merchantBank.getId(), Product.USSD_MERCHANT_PAYMENT);
                   } catch (Exception e) {
                       log.error("could not get acct for merchant bank {}", merchantBank, e);
                   }
               }

                if (null != merchantAcct) {
                   partyAccountMap.put(merchantBank.getCbnBankCode(), merchantAcct);
                    merchantBankShare = config.getMerchantBankShare().multiply(totalFee).setScale(2, ROUNDING_MODE);
                    if (map.containsKey(merchantAcct)) {
                        map.put(merchantAcct, map.get(merchantAcct).add(merchantBankShare));
                    } else {
                        map.put(merchantAcct, merchantBankShare);
                    }
                } else
                    log.warn("could not get acct for merchant bank {} for product {}", merchantBank.getName(), Product.USSD_MERCHANT_PAYMENT);
            } else
                log.warn("could not get merchant bank with code {}", dt.getCreditTransaction().getInstitution().getBankCode());

//            log.trace("done adding merchant bank share");
        }

        if (null != config.getUssdAggregatorShare() && config.getUssdAggregatorShare().compareTo(BigDecimal.ZERO) > 0) {
//            log.trace("about adding ussd aggregator share");
            String aggCode = dt.getAggregator().getAggregatorCode();
            BankAccount ussdAggAcct = getForParty(aggCode);

            if( null == ussdAggAcct) {
                ussdAggAcct = fromAccountDetail(merchantPaymentTransactionRepository.findForAggregatorId(dt.getAggregator().getAggregatorId()));
            }
            if (null != ussdAggAcct) {
                partyAccountMap.put(aggCode, ussdAggAcct);
                ussdAggregatorShare = config.getUssdAggregatorShare().multiply(totalFee).setScale(2, ROUNDING_MODE);
                if (map.containsKey(ussdAggAcct)) {
                    map.put(ussdAggAcct, map.get(ussdAggAcct).add(ussdAggregatorShare));
                } else {
                    map.put(ussdAggAcct, ussdAggregatorShare);
                }
            } else
                log.warn("could not get acct for ussd aggr {} for product {}", dt.getAggregator().getAggregatorName(),
                        Product.USSD_MERCHANT_PAYMENT);
//            log.trace("done adding ussd aggregator share");

        }

        if (null != config.getMerchantIntroducerShare() && config.getMerchantIntroducerShare().compareTo(BigDecimal.ZERO) > 0) {
            List<Organization> aggregators;
            long merchantId = dt.getMerchant().getMerchantId();
            try {
//                log.trace("about adding aggregator share");
                aggregators = organizationRepository.findAggregatorForMerchantPaymentMerchant(merchantId);

              if( null != aggregators && !aggregators.isEmpty()) {
                  Organization aggregator = aggregators.get(0);
                  if (null != aggregator) {
                      log.trace("mCash merchant introducer {}, {}", aggregator.getId(), aggregator.getName());
                      merchantIntroducerMap.put(merchantId, aggregator.getName());

                      String aggCode = null;
                      if( aggregator instanceof Bank)
                          aggCode = ((Bank)aggregator).getCbnBankCode();
                      else if( aggregator instanceof Aggregator)
                          aggCode = ((Aggregator)aggregator).getCode();

                      BankAccount merchantPayAcct = getForParty(aggCode);

                      if( null == merchantPayAcct) {
                          try {
                              merchantPayAcct = bankAccountRepository.findByOrganizationAndProductCode(aggregator.getId(), Product.USSD_MERCHANT_PAYMENT);
                          } catch (Exception e) {
                              log.error("could not get acct for biller aggr. {}", aggregator, e);
                          }
                      }


                      if (null != merchantPayAcct) {
                          partyAccountMap.put(aggCode, merchantPayAcct);
                          BigDecimal introducerShare = config.getMerchantIntroducerShare().multiply(totalFee).setScale(2, ROUNDING_MODE);
                          if (map.containsKey(merchantPayAcct)) {
                              map.put(merchantPayAcct, map.get(merchantPayAcct).add(introducerShare));
                          } else {
                              map.put(merchantPayAcct, introducerShare);
                          }
                      } else
                          log.warn("could not get introducer share for {}, product {}", aggregator.getName(), Product.USSD_MERCHANT_PAYMENT);

                  }
              }
            } catch (Exception e) {
                log.error("could not get aggregator for Merchant Payment merchant with ID {}", merchantId, e);
                throw new RuntimeException(e);
            }
//            log.trace("done adding aggregator share");
        }

        if (null != config.getTelcoShare() && config.getTelcoShare().compareTo(BigDecimal.ZERO) > 0) {
            String telcoCode = dt.getTelco().getTelcoCode();
            BankAccount telcoAcct = getForParty(telcoCode);

            if( null == telcoAcct) {
                telcoAcct = fromAccountDetail(merchantPaymentTransactionRepository.findForTelcoId(dt.getTelco().getTelcoId()));
            }
//            log.trace("about adding telco share");
            if (null != telcoAcct) {
                partyAccountMap.put(telcoCode, telcoAcct);
                telcoShare = config.getTelcoShare().multiply(totalFee).setScale(2, ROUNDING_MODE);

                if (map.containsKey(telcoAcct)) {
                    map.put(telcoAcct, map.get(telcoAcct).add(telcoShare));
                } else {
                    map.put(telcoAcct, telcoShare);
                }
            } else
                log.warn("could not get acct for telco {}", dt.getTelco().getTelcoName());
//            log.trace("done adding telco share");
        }
        if (null != config.getSchemeRoyaltyShare() && config.getSchemeRoyaltyShare().compareTo(BigDecimal.ZERO) > 0) {

            Telco royalty = null;
            BankAccount telcoAccount = getForParty(config.getSchemeRoyaltyCode());

            if( null == telcoAccount) {


                //            log.trace("about getting royalty scheme telco");
                try {
                    royalty = telcoRepository.findByTelcoCode(config.getSchemeRoyaltyCode());
//                log.trace("done getting royalty scheme telco");
                } catch (Exception e) {
                    log.error("could not get telco for royalty scheme", e);
                }

                if (null != royalty) {
                    telcoAccount = fromAccountDetail(royalty.getAccountDetail());
                }

            }

            //                log.trace("about getting royalty scheme account details");
//
//                log.trace("done getting royalty scheme account details");
            if (null != telcoAccount) {
                partyAccountMap.put(config.getSchemeRoyaltyCode(), telcoAccount);
                BigDecimal royaltyShare = totalFee.multiply(config.getSchemeRoyaltyShare()).setScale(2, ROUNDING_MODE);
                if (map.containsKey(telcoAccount)) {
                    map.put(telcoAccount, map.get(telcoAccount).add(royaltyShare));
                } else {
                    map.put(telcoAccount, royaltyShare);
                }
            } else
                log.warn("no acct for telco royalty {}", royalty.getTelcoName());


        }
    }

    private String buildTransactionDetailReport(List<DebitTransaction> transactions, MerchantPaymentSharingConfig config) {
        if (null == transactions || transactions.isEmpty()) {
            log.trace("no transactions were passed in");
            return null;
        }

        CSVFormat format = CSVFormat.EXCEL;
        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(Arrays.asList(DETAIL_HEADER));
            for (DebitTransaction t : transactions) {
                List<String> items = new ArrayList<>();
                items.add("'" + t.getSessionID());
                items.add("'" + t.getCreditTransaction().getSessionID());
                items.add(t.getMerchant().getMerchantName());
                items.add(t.getMerchant().getMerchantCode());
                items.add(t.getReferenceCode());
                items.add(t.getPaymentReference());
                items.add(t.getTelco().getTelcoName());
                items.add(t.getAggregator().getAggregatorName());
                items.add(t.getInstitution().getInstitutionName());
                items.add(t.getCreditTransaction().getInstitution().getInstitutionName());
                long merchantId = t.getMerchant().getMerchantId();
                String merchant = "";
                if (merchantIntroducerMap.containsKey(merchantId)) {
                    merchant = merchantIntroducerMap.get(merchantId);
                }

                items.add(merchant);
                items.add(t.getAmount().setScale(2, ROUNDING_MODE).toPlainString());

                BigDecimal totalFee = t.getFee().add(t.getCreditTransaction().getFee() == null
                        ? BigDecimal.ZERO : t.getCreditTransaction().getFee());

                items.add(totalFee.setScale(2, ROUNDING_MODE).toPlainString());
                BigDecimal nibssShare = null != config.getNibssShare() ? config.getNibssShare().multiply(totalFee).setScale(2, ROUNDING_MODE) : BigDecimal.ZERO;
                items.add(nibssShare.toPlainString());

                BigDecimal ussdAggShare = null != config.getUssdAggregatorShare() ? config.getUssdAggregatorShare().multiply(totalFee).setScale(2, ROUNDING_MODE) : BigDecimal.ZERO;
                items.add(ussdAggShare.toPlainString());

                BigDecimal telcoShare = null != config.getTelcoShare() ? config.getTelcoShare().multiply(totalFee).setScale(2, ROUNDING_MODE) : BigDecimal.ZERO;
                items.add(telcoShare.toPlainString());

                BigDecimal royaltyShare = null != config.getSchemeRoyaltyShare() ? config.getSchemeRoyaltyShare().multiply(totalFee).setScale(2, ROUNDING_MODE) : BigDecimal.ZERO;
                items.add(royaltyShare.toPlainString());

                BigDecimal payerBankShare = null != config.getPayerBankShare() ? config.getPayerBankShare().multiply(totalFee).setScale(2, ROUNDING_MODE) : BigDecimal.ZERO;
                items.add(payerBankShare.toPlainString());

                BigDecimal merchantBankShare = null != config.getMerchantBankShare() ? config.getMerchantBankShare().multiply(totalFee).setScale(2, ROUNDING_MODE) : BigDecimal.ZERO;
                items.add(merchantBankShare.toPlainString());

                BigDecimal merchantIntroducerShare = null != config.getMerchantIntroducerShare() ? config.getMerchantIntroducerShare().multiply(totalFee).setScale(2, ROUNDING_MODE) : BigDecimal.ZERO;
                items.add(merchantIntroducerShare.toPlainString());

                printer.printRecord(items);
            }

            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("could not build transaction details CSV", e);
        }

    }

    private BankAccount fromAccountDetail(final AccountDetail acctDetail) {
        if (null == acctDetail) {
            return null;
        }
        BankAccount temp = new BankAccount();
        temp.setAccountName(acctDetail.getAccountName());
        temp.setAccountNumber(acctDetail.getAccountNumber());
        temp.setBankCode(acctDetail.getInstitution().getBankCode());
        return temp;
    }


    private BankAccount getForParty(String partyCode) {
        if( null == partyCode || partyCode.trim().isEmpty())
            return null;
        return partyAccountMap.get(partyCode);
    }
}

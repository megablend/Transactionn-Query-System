package com.nibss.tqs.ussd.billing;

import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.BankAccountRepository;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.ussd.dto.*;
import com.nibss.tqs.util.BillingHelper;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Emor on 8/18/16.
 */
@Component("ussdBillPaymentBillingProvider")
@Slf4j
@Transactional
public class UssdBillPaymentBillingProvider implements BillingProvider {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private BillingHelper billingHelper;

    @Autowired
    private BillPaymentReportHelper reportHelper;
    
    @Autowired
    private BankAccountRepository bankAccountRepository;

    private static final String[] DETAIL_HEADER = {
        "SOURCE SESSION ID", "SOURCE BANK CODE", "DESTINATION BANK CODE", "TRANSACTION DATE",
            "MERCHANT NAME", "MERCHANT CODE", "TRXN. REF", "AMOUNT", "TRANSACTION FEE",
            "COLLECTING BANK SHARE", "NIBSS SHARE", "AGGREGATOR SHARE", "BILLER BANK SHARE",
            "USSD AGGREGATOR SHARE", "TELCO SHARE"

    };

    @Override
    @Transactional
    public Path getBillingZipFile(List<? extends Serializable> trxn, BillingPeriod billingPeriod) throws RuntimeException, IOException {
        List<UssdTransaction> transactions = trxn.stream().map(t -> (UssdTransaction) t).collect(Collectors.toList());

        log.trace("No. of transactions for billing: {}", transactions.size());

        //group transactions by source bank
        log.trace("about grouping transactions by source bank");
        Map<String, List<UssdTransaction>> trxnsBySourceBank = transactions.stream().collect(Collectors.groupingBy(t -> t.getSourceBankCode()));
        log.trace("done grouping transactions by source bank");

        log.trace("about generating smartdet file");
        Map<String, BigDecimal> smartDetMap = new HashMap<>();
        trxnsBySourceBank.forEach((k, v) -> smartDetMap.put(k, v.stream().map(a -> a.getTransactionFee()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO)));

        String smartdetContent = billingHelper.getSmartDetFile(smartDetMap, appSettings.ussdBillPaymentSmartdetCode());
        log.trace("done generating smartdet file");

        //group transactions by ussd biller so that we can apply biller sharing configs
        log.trace("about grouping transactions by ussd biller");
        Map<UssdBiller, List<UssdTransaction>> trxnsByBiller = transactions.stream().collect(Collectors.groupingBy(t -> t.getUssdBiller()));
        log.trace("done grouping transactions by ussd biller");

        Map<BankAccount, BigDecimal> sharedSums = new HashMap<>();

        log.trace("about sharing transaction fee for transactions");
        StopWatch timer = new StopWatch();
        timer.start();
        trxnsByBiller.forEach((k, v) -> doSharing(k, v, sharedSums));
        timer.stop();
        log.trace("done sharing transaction fee for transactions. Time taken in ms: {}", timer.getTotalTimeMillis());

        log.trace("about generating nibss payment file");
        String nibssPayFile = billingHelper.getNIBSSPayPaymentFile(sharedSums, appSettings.ussdBillPaymentNarration(), appSettings.ussdBillPaymentPayerName());
        log.trace("done generating nibss payment file");

        billingHelper.createPaymentProductFolder(Product.USSD_BILL_PAYMENT);

        DateFormat dtFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH);

        String strDate = dtFormat.format(new Date());
        String zipFileName = String.format("USSDBillPaymentBilling_%s_%s.zip", strDate,billingPeriod);

        log.trace("about generating zip payment file");
        timer = new StopWatch();
        timer.start();
        Path ussdBillingFilePath = Paths.get(appSettings.billingPaymentFolder(), Product.USSD_BILL_PAYMENT, zipFileName);

        Map<String, String> filesMap = new HashMap<>();

        String smarDetFileName = String.format("UssdBillPaymentSmartDet_%s.txt", strDate);
        String nibssPaymentFileName = String.format("UssdBillPaymentNIBSSPaymentFile_%s.txt", strDate);
        String transactionDetailFile = String.format("TransactionDetails_%s.csv", strDate);

        filesMap.put(smarDetFileName, smartdetContent);
        filesMap.put(nibssPaymentFileName, nibssPayFile);
        filesMap.put(transactionDetailFile, buildTransactionDetailReport(transactions));

        //write zip file to disc
        billingHelper.writePaymentZipFile(filesMap, ussdBillingFilePath);
        timer.stop();
        log.trace("done generating zip payment file. Time taken in ms: {}", timer.getTotalTimeMillis());

        return ussdBillingFilePath;
    }

    @Override
    @Transactional
    public void generatePartyReports(List<? extends Serializable> trxns,BillingPeriod billingPeriod) {

        List<UssdTransaction> transactions = trxns.stream().map(t -> (UssdTransaction) t).collect(Collectors.toList());
        try {
            reportHelper.generateReports(transactions);
        } catch (Exception e) {
            log.error("could not generate reports", e);
        }
    }

    @Override
    public void cleanUp() {

    }

    private String buildTransactionDetailReport(List<UssdTransaction> transactions) {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.EXCEL;


        Map<UssdBiller, List<UssdTransaction>> txnMap = transactions.stream().collect(Collectors.groupingBy(t -> t.getUssdBiller()));

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(Arrays.asList(DETAIL_HEADER));


            for( UssdBiller b : txnMap.keySet()) {

                UssdFeeSharingConfig feeConfig = b.getFeeSharingConfig();

                for (UssdTransaction t : txnMap.get(b)) {
                    List<String> items = new ArrayList<>();

                    items.add( "'"+ t.getSourceSessionId());
                    items.add(t.getSourceBankCode());
                    items.add(t.getDestinationBankCode());
                    items.add(DATE_FORMAT_THREAD_LOCAL.get().format(t.getRequestDate()));
                    items.add(t.getUssdBiller().getName());
                    items.add(t.getUssdBiller().getMerchantCode());
                    items.add(t.getId());
                    items.add(t.getAmount().setScale(2, ROUNDING_MODE).toPlainString());
                    items.add(t.getTransactionFee().setScale(2, ROUNDING_MODE).toPlainString());

                    BigDecimal colBankShare = getShare(feeConfig.getCollectingBankShare(), t.getTransactionFee(),
                            feeConfig.isPercentage());
                    items.add(colBankShare.toPlainString());

                    BigDecimal nibssShare = getShare(feeConfig.getNibssShare(), t.getTransactionFee(),
                            feeConfig.isPercentage());
                    items.add(nibssShare.toPlainString());

                    BigDecimal aggShare = getShare(feeConfig.getAggregatorShare(), t.getTransactionFee(), feeConfig.isPercentage());
                    items.add(aggShare.toPlainString());

                    BigDecimal billerBankShare = getShare(feeConfig.getBillerBankShare(), t.getTransactionFee(), feeConfig.isPercentage());
                    items.add(billerBankShare.toPlainString());

                    BigDecimal ussdAggShare = getShare(feeConfig.getUssdAggregatorShare(), t.getTransactionFee(), feeConfig.isPercentage());
                    items.add(ussdAggShare.toPlainString());

                    BigDecimal telcoShare = getShare(feeConfig.getTelcoShare(), t.getTransactionFee(), feeConfig.isPercentage());
                    items.add(telcoShare.toPlainString());

                    printer.printRecord(items);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("could not generate transaction detail report", e);
        }

        return writer.toString();
    }

    private void doSharing(UssdBiller biller, List<UssdTransaction> transactions, Map<BankAccount, BigDecimal> map) {
        UssdFeeSharingConfig config = biller.getFeeSharingConfig();

        for (UssdTransaction t : transactions) {

            BigDecimal aggregatorShare;
            BigDecimal collectingBankShare;
            BigDecimal billerBankShare;
            BigDecimal ussdAggShare;
            BigDecimal telcoShare;

            BigDecimal fee = t.getTransactionFee();
            if (config.isPercentage()) {
                aggregatorShare = doPercentage(fee, config.getAggregatorShare()).setScale(2, ROUNDING_MODE);
                collectingBankShare = doPercentage(fee, config.getCollectingBankShare()).setScale(2, ROUNDING_MODE);
                billerBankShare = doPercentage(fee, config.getBillerBankShare()).setScale(2, ROUNDING_MODE);
                ussdAggShare = doPercentage(fee, config.getUssdAggregatorShare()).setScale(2, ROUNDING_MODE);
                telcoShare = doPercentage(fee, config.getTelcoShare()).setScale(2, ROUNDING_MODE);
            } else {
                aggregatorShare = config.getAggregatorShare();
                collectingBankShare = config.getCollectingBankShare();
                billerBankShare = config.getBillerBankShare();
                ussdAggShare = config.getUssdAggregatorShare();
                telcoShare = config.getTelcoShare();
            }

            if (notEmpty(aggregatorShare)) {
                doAggregatorShare(biller, map, aggregatorShare);
            }

            if (notEmpty(collectingBankShare)) {
                doCollectingBankShare(map, t, collectingBankShare);

            }

            if (notEmpty(billerBankShare)) {
                doBillerBankShare(map, config, billerBankShare);
            }

            if (notEmpty(ussdAggShare)) {
                log.trace("about adding ussd aggregator share");
                UssdAggregator agg = t.getUssdAggregator();
                BankAccount acct = new BankAccount(agg.getAccountName(), agg.getAccountNumber(), agg.getBankCode());
                if (map.containsKey(acct)) {
                    map.put(acct, map.get(acct).add(ussdAggShare));
                } else {
                    map.put(acct, ussdAggShare);
                }
                log.trace("done adding ussd aggregator share");
            }

            if (notEmpty(telcoShare)) {
                log.trace("about adding telco share");
                UssdTelco telco = t.getTelco();
                BankAccount telcoAcct = new BankAccount(telco.getAccountName(), telco.getAccountNumber(), telco.getBankCode());
                if (map.containsKey(telcoAcct)) {
                    map.put(telcoAcct, map.get(telcoAcct).add(telcoShare));
                } else {
                    map.put(telcoAcct, telcoShare);
                }
                log.trace("done adding telco share");
            }
        }
    }

    private void doBillerBankShare(Map<BankAccount, BigDecimal> map, UssdFeeSharingConfig config, BigDecimal billerBankShare) {
        log.trace("about getting biller bank details");
        Bank billerBank = bankRepository.findByCode(config.getBillerBankCode());
        if (billerBank != null) {
            BankAccount acct = null;
            try {
                acct = bankAccountRepository.findByOrganizationAndProductCode(billerBank.getId(), Product.EBILLSPAY);
            } catch (Exception e) {
                log.error("could not get acct for aggr. {}", billerBank, e);
            }
           
            if (acct != null) {
                if (map.containsKey(acct)) {
                    map.put(acct, map.get(acct).add(billerBankShare));
                } else {
                    map.put(acct, billerBankShare);
                }
            } else {
                log.warn("an account is yet to be maintained for this product for  bank {}", billerBank.getName());
            }
        } else {
            log.trace("could not get biller bank with code {}", config.getBillerBankCode());
        }
    }

    private void doCollectingBankShare(Map<BankAccount, BigDecimal> map, UssdTransaction t, BigDecimal collectingBankShare) {
        log.trace("about getting details for collecting bank with code {}", t.getSourceBankCode());
        Bank collectingBank = bankRepository.findByCode(t.getSourceBankCode());
        if (null != collectingBank) {
            BankAccount bankAcct = null;
            try {
                bankAcct = bankAccountRepository.findByOrganizationAndProductCode(collectingBank.getId(), Product.EBILLSPAY);
            } catch (Exception e) {
                log.error("could not get acct for aggr. {}", collectingBank, e);
            }

            if (null != bankAcct) {
                if (map.containsKey(bankAcct)) {
                    map.put(bankAcct, map.get(bankAcct).add(collectingBankShare));
                } else {
                    map.put(bankAcct, collectingBankShare);
                }

            } else {
                log.warn("could not get bank account for bank with code {}", t.getSourceBankCode());
            }
        } else {
            log.trace("could not get collecting bank");
        }
    }

    private void doAggregatorShare(UssdBiller biller, Map<BankAccount, BigDecimal> map, BigDecimal aggregatorShare) {
        log.trace("about getting aggregator for ussd biller {}", biller.getName());

        List<Organization> aggregators = organizationRepository.findAggregatorForUssdBiller(biller.getMerchantCode());
        if( null == aggregators || aggregators.isEmpty())
            return;

        Organization aggregator = aggregators.get(0);
        if (null != aggregator) {
           BankAccount theAcct = null;
            try {
                theAcct = bankAccountRepository.findByOrganizationAndProductCode(aggregator.getId(), Product.EBILLSPAY);
            } catch (Exception e) {
                log.error("could not get acct for aggr. {}", aggregator, e);
            }

            if (null != theAcct) {
                if (map.containsKey(theAcct)) {
                    map.put(theAcct, map.get(theAcct).add(aggregatorShare));
                } else {
                    map.put(theAcct, aggregatorShare);
                }

            } else {
                log.warn("no bank account has  been maintained for the aggregator {} for this product {}", aggregator.getName(), Product.USSD_BILL_PAYMENT);
            }
        }
    }

    private BigDecimal doPercentage(BigDecimal transactionFee, BigDecimal commission) {
        if (commission == null || commission.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return transactionFee.multiply(commission);
    }

    private boolean notEmpty(final BigDecimal commission) {
        if (null == commission || commission.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return true;
    }

}

package com.nibss.tqs.ebillspay.billing;

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

import com.nibss.tqs.ebillspay.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.Bank;
import com.nibss.tqs.core.entities.BankAccount;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.repositories.BankAccountRepository;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.util.BillingHelper;
import com.nibss.tqs.util.Utility;

/**
 *
 * @author Emor
 *
 * the billing provider for billers who's fees are not taken at transaction time
 * and that billing is passed at the end of the week
 *
 */
@Component("customBillerBillingProvider")
@Slf4j
public class CustomBillerBillingProvider implements BillingProvider {

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private BillingHelper billingHelper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EbillsPayBillingReportHelper billingReportHelper;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private static final String[] HEADERS = {
        "SESSION ID", "BILLER NAME", "SOURCE BANK","BRANCH CODE", "AGGREGATOR", "BILLER BANK",
            "TRANSACTION FEE", "SOURCE BANK FEE", "AGGREGATOR FEE", "NIBSS FEE", "BILLER BANK FEE"
            , "TRANSACTION DATE"
    };

    private Map<Integer, String> aggrMap = new HashMap<>();
    private Map<Integer, String> billerBankMap = new HashMap<>();

    @Override
    @Transactional
    public Path getBillingZipFile(List<? extends Serializable> transactions, BillingPeriod billingPeriod) throws RuntimeException, IOException {
        List<EbillspayTransaction> txns = transactions.stream().map(t -> (EbillspayTransaction) t).collect(Collectors.toList());

        //group by biller
        Map<Biller, List<EbillspayTransaction>> byBiller = txns.stream().collect(Collectors.groupingBy(t -> t.getBaseTransaction().getBiller()));

        Map<BankAccount, BigDecimal> debitMap = new HashMap<>();

        //for each biller, get the Organization's bank account.
        //for each biller transaction, compute the transaction fee that applies
        byBiller.forEach((k, v) -> {
            EbillsPayTransactionFee tFee = k.getEbillsPayTransactionFee();
            BankAccount debitAcct = getBillerAccount(k);
            BigDecimal totalDebitAmt = v.stream().map(t -> computeTransactionFee(t, tFee)).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);
            debitMap.put(debitAcct, totalDebitAmt);
        });

        //TODO: generate debit file for billers
        Map<BankAccount, BigDecimal> sharedSums = new HashMap<>();

        byBiller.forEach((k, v) -> {

            EbillsBillingConfiguration b = k.getEbillsBillingConfigurations();
            v.forEach(t -> {
                doAggregatorShare(b, t, sharedSums);
                doBillerBankShare(b, t, sharedSums);
                doCollectingBankShare(b, t, sharedSums);
            });

        });

        String nibssPayFile = billingHelper.getNIBSSPayPaymentFile(sharedSums, appSettings.ebillspayCommissionNarration(), appSettings.ebillspayPayer());

        billingHelper.createPaymentProductFolder(Product.EBILLSPAY);

        DateFormat dtFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH);

        String strDate = dtFormat.format(new Date());
        String zipFileName = String.format("eBillsPaymentCustomBillerBilling_%s_%s.zip", strDate,billingPeriod);

        String billerDebitContent = billingHelper.getHAWKDebitFile(debitMap, appSettings.debitNarration());

        Path customBillingFilePath = Paths.get(appSettings.billingPaymentFolder(), Product.EBILLSPAY, zipFileName);

        Map<String, String> filesMap = new HashMap<>();

        String debitFileName = String.format("eBillsPayCustomBillerDebitFile_%s.csv", strDate);
        String nibssPaymentFileName = String.format("eBillsPaymentCustomBillerBillingNIBSSPaymentFile_%s.txt", strDate);
        String transactionDetailFile = String.format("TransactionDetails_%s.csv", strDate);

        filesMap.put(debitFileName, billerDebitContent);
        filesMap.put(nibssPaymentFileName, nibssPayFile);
        filesMap.put(transactionDetailFile, buildTransactionDetail(byBiller));

        billingHelper.writePaymentZipFile(filesMap, customBillingFilePath);

        return customBillingFilePath;
    }

    @Override
    @Transactional
    public void generatePartyReports(List<? extends Serializable> transactions, BillingPeriod billingPeriod) {
        List<BaseTransaction> txns = transactions.stream().map(t -> ((EbillspayTransaction)t).getBaseTransaction()).collect(Collectors.toList());
        try {
            billingReportHelper.generateReports(txns);
        } catch (Exception e) {
            log.error("could nt generate custom billing report", e);
        }

    }

    @Override
    public void cleanUp() {
        aggrMap.clear();
        billerBankMap.clear();

    }

    private String buildTransactionDetail(Map<Biller, List<EbillspayTransaction>> txnMap) {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.EXCEL;

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord(Arrays.asList(HEADERS));

            for (Biller b : txnMap.keySet()) {
                EbillsBillingConfiguration config = b.getEbillsBillingConfigurations();
                for (EbillspayTransaction t : txnMap.get(b)) {
                    List<String> items = new ArrayList<>();
                    items.add(String.format("'%s", t.getBaseTransaction().getSessionId()));
                    items.add(b.getName());
                    items.add(t.getBaseTransaction().getSourceBank() == null ? "" : t.getBaseTransaction().getSourceBank().getName());
                    items.add(t.getBaseTransaction().getBranchCode());
                    items.add(aggrMap.get(b.getId()) == null ? "" : aggrMap.get(b.getId()));
                    items.add(billerBankMap.get(b.getId()) == null ? "" : billerBankMap.get(b.getId()));
                    items.add(t.getBaseTransaction().getTransactionFee().setScale(2, BillingProvider.ROUNDING_MODE).toPlainString());
                    BigDecimal colBankShare = Utility.getShare(config.getCollectingBankShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage()).setScale(2, BillingProvider.ROUNDING_MODE);
                    items.add(colBankShare.toPlainString());
                    BigDecimal aggrShare = Utility.getShare(config.getAggregatorShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage()).setScale(2, BillingProvider.ROUNDING_MODE);
                    items.add(aggrShare.toPlainString());
                    BigDecimal nibssShare = Utility.getShare(config.getNibssShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage()).setScale(2, BillingProvider.ROUNDING_MODE);
                    items.add(nibssShare.toPlainString());
                    BigDecimal billerBankShare = Utility.getShare(config.getBillerBankShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage()).setScale(2, BillingProvider.ROUNDING_MODE);
                    items.add(billerBankShare.toPlainString());
                    items.add(DATE_FORMAT_THREAD_LOCAL.get().format(t.getBaseTransaction().getTransactionDate()));

                    printer.printRecord(items);

                }
            }

        } catch (IOException e) {
            throw new RuntimeException("could nt gen. txn details file", e);
        }

        return writer.toString();
    }

    private void doAggregatorShare(EbillsBillingConfiguration config, EbillspayTransaction t, Map<BankAccount, BigDecimal> map) {
        if (Utility.isEmptyBigDecimal(config.getAggregatorShare())) {
            return;
        }

        List<Organization> aggrs = organizationRepository.findAggregatorForEbillsPayBiller(t.getBaseTransaction().getBiller().getId());
        if (aggrs == null || aggrs.isEmpty()) {
            return;
        }

        Organization aggr = aggrs.get(0);
        aggrMap.put(t.getBaseTransaction().getBiller().getId(), aggr.getName());

        BankAccount acct = null;
        try {
            acct = bankAccountRepository.findByOrganizationAndProductCode(aggr.getId(), Product.EBILLSPAY);
        } catch (Exception e) {
            log.error("could not get acct for aggr. {}", aggr, e);
        }
        if (acct == null) {
            log.warn("could not get aggregator acct: {}", aggr.getName());
            return;
        }

        BigDecimal share = config.getAggregatorShare();
        if (config.isPercentage()) {
            share = config.getAggregatorShare().multiply(t.getBaseTransaction().getTransactionFee());
        }

        share = share.setScale(2, ROUNDING_MODE);
        if (map.containsKey(acct)) {
            map.put(acct, map.get(acct).add(share));
        } else {
            map.put(acct, share);
        }
    }

    private void doCollectingBankShare(EbillsBillingConfiguration config, EbillspayTransaction t, Map<BankAccount, BigDecimal> map) {
        if (Utility.isEmptyBigDecimal(config.getCollectingBankShare())) {
            return;
        }

        Bank bank = bankRepository.findByCode(t.getBaseTransaction().getSourceBank().getCode());

        if (null == bank) {
            return;
        }

        BankAccount acct = null;
        try {
            acct = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.EBILLSPAY);
        } catch (Exception e) {
            log.error("could not get acct for bank. {}", bank, e);
        }

        if (acct == null) {
            log.warn("no acct for collecting bank {}", bank.getName());
            return;
        }

        BigDecimal share = config.getCollectingBankShare();
        if (config.isPercentage()) {
            share = config.getCollectingBankShare().multiply(t.getBaseTransaction().getTransactionFee());
        }

        share = share.setScale(2, ROUNDING_MODE);

        if (map.containsKey(acct)) {
            map.put(acct, map.get(acct).add(share));
        } else {
            map.put(acct, share);
        }
    }

    private void doBillerBankShare(EbillsBillingConfiguration config, EbillspayTransaction t, Map<BankAccount, BigDecimal> map) {
        if (Utility.isEmptyBigDecimal(config.getBillerBankShare()) || null == config.getBillerBankCode()) {
            return;
        }
        Bank bank = bankRepository.findByCode(config.getBillerBankCode());

        if (null == bank) {
            return;
        }

        billerBankMap.put(t.getBaseTransaction().getBiller().getId(), bank.getName());
        BankAccount acct = null;
        try {
            acct = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.EBILLSPAY);
        } catch (Exception e) {
            log.error("could not get acct for biller bank. {}", bank, e);
        }
        if (acct == null) {
            return;
        }

        if (acct == null) {
            log.warn("no acct for biller bank {}", bank.getName());
            return;
        }

        BigDecimal share = config.getBillerBankShare();
        if (config.isPercentage()) {
            share = config.getBillerBankShare().multiply(t.getBaseTransaction().getTransactionFee());
        }

        share = share.setScale(2, ROUNDING_MODE);
        if (map.containsKey(acct)) {
            map.put(acct, map.get(acct).add(share));
        } else {
            map.put(acct, share);
        }

    }

    private BankAccount getBillerAccount(final Biller biller) {
        List<Organization> orgs = organizationRepository.findEbillspayMerchant(biller.getId());

        if (orgs == null || orgs.isEmpty()) {
            return null;
        }
        Organization org = orgs.get(0);
        BankAccount acct = org.getBankAccounts().stream().filter(t -> t.getProduct().getCode().equals(Product.EBILLSPAY)).findFirst().orElse(null);

        if( null == acct) {
            log.warn("no acct for biller {} maintained. ", biller.getName());
        }
        return acct;
    }

    private BigDecimal computeTransactionFee(final EbillspayTransaction t, EbillsPayTransactionFee fee) {
        BigDecimal calAmt;
        if (!fee.isPercentage()) {

            calAmt = fee.getFee();
            calAmt = calAmt.setScale(2, ROUNDING_MODE);
            t.getBaseTransaction().setTransactionFee(calAmt);
            return calAmt;
        }

        calAmt = fee.getFee().multiply(t.getBaseTransaction().getAmount());
        if (!Utility.isEmptyBigDecimal(fee.getAmountFloor())) {
            if (calAmt.compareTo(fee.getAmountFloor()) < 0) {
                calAmt = fee.getAmountFloor();
            }
        } else if (!Utility.isEmptyBigDecimal(fee.getAmountCap())) {
            if (calAmt.compareTo(fee.getAmountCap()) > 0) {
                calAmt = fee.getAmountCap();
            }
        }

        calAmt = calAmt.setScale(2, ROUNDING_MODE);
        t.getBaseTransaction().setTransactionFee(calAmt);
        return calAmt;
    }

}

package com.nibss.tqs.ebillspay.billing;

import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.Bank;
import com.nibss.tqs.core.entities.BankAccount;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.repositories.BankAccountRepository;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ebillspay.dto.EbillsBillingConfiguration;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.util.BillingHelper;
import com.nibss.tqs.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
 * Created by eoriarewo on 9/15/2016.
 */
@Component("ebillsTransactionTimeBillingProvider")
@Slf4j
public class TransactionTimeBillingProvider implements BillingProvider {

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private BillingHelper billingHelper;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private EbillsPayBillingReportHelper billingReportHelper;

    private Map<Integer, String> aggrMap = new HashMap<>();
    private Map<Integer, String> billerBankMap = new HashMap<>();


    private static final String[] HEADERS = {
            "SESSION ID", "BILLER NAME", "SOURCE BANK", "BRANCH CODE","AGGREGATOR", "BILLER BANK", "TRANSACTION FEE", "SOURCE BANK FEE", "AGGREGATOR FEE", "NIBSS FEE", "BILLER BANK FEE",
            "TRANSACTION DATE"
    };

    @Override
    @Transactional
    public Path getBillingZipFile(List<? extends Serializable> transactions, BillingPeriod billingPeriod) throws RuntimeException, IOException {
        List<EbillspayTransaction> trxns = transactions.stream().map(t -> (EbillspayTransaction) t).collect(Collectors.toList());

        //grp by source bank
        Map<String, List<EbillspayTransaction>> byBank = trxns.stream()
                .collect(Collectors.groupingBy(t -> t.getBaseTransaction().getSourceBank().getCode()));

        Map<String, BigDecimal> smartDetMap = new HashMap<>();
        byBank.forEach((k, v) -> {
            smartDetMap.put(k, v.stream().map(t -> t.getBaseTransaction().getTransactionFee()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO));
        });

        String smartDetContent = billingHelper.getSmartDetFile(smartDetMap, appSettings.ebillspaySmartDetCode());

        Map<Biller, List<EbillspayTransaction>> byBiller = trxns.stream().collect(Collectors.groupingBy(t -> t.getBaseTransaction().getBiller()));

        Map<BankAccount, BigDecimal> sharedSums = new HashMap<>();

        byBiller.forEach((k, v) -> {
            EbillsBillingConfiguration config = k.getEbillsBillingConfigurations();
            v.forEach(t -> {
                doAggregatorShare(t, config, sharedSums);
                doBillerBankShare(t, config, sharedSums);
                doCollectingBankShare(t, config, sharedSums);
            });
        });

        String nibssPayFile = billingHelper.getNIBSSPayPaymentFile(sharedSums, appSettings.ebillspayCommissionNarration(), appSettings.ebillspayPayer());

        billingHelper.createPaymentProductFolder(Product.EBILLSPAY);

        DateFormat dtFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH);

        String strDate = dtFormat.format(new Date());
        String zipFileName = String.format("eBillsPaymentNormalBilling_%s_%s.zip", strDate, billingPeriod);

        Path ebillsPayPath = Paths.get(appSettings.billingPaymentFolder(), Product.EBILLSPAY, zipFileName);

        Map<String, String> filesMap = new HashMap<>();

        String smarDetFileName = String.format("eBillsPaymentNormalSmartDet_%s.txt", strDate);
        String nibssPaymentFileName = String.format("eBillsPaymentNormalNIBSSPaymentFile_%s.txt", strDate);
        String transactionDetailFile = String.format("TransactionDetails_%s.csv", strDate);

        filesMap.put(smarDetFileName, smartDetContent);
        filesMap.put(nibssPaymentFileName, nibssPayFile);
        filesMap.put(transactionDetailFile, buildTransactionDetail(byBiller));

        billingHelper.writePaymentZipFile(filesMap, ebillsPayPath);

        return ebillsPayPath;
    }

    @Override
    @Transactional
    public void generatePartyReports(List<? extends Serializable> transactions, BillingPeriod billingPeriod) {

        List<BaseTransaction> txns = transactions.stream().map(t -> ((EbillspayTransaction) t).getBaseTransaction()).collect(Collectors.toList());
        try {
            billingReportHelper.generateReports(txns);
        } catch (Exception e) {
            log.error("could not generate party reports", e);
        }
    }

    @Override
    public void cleanUp() {
        aggrMap.clear();
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
                    items.add(t.getBaseTransaction().getTransactionFee().toPlainString());
                    BigDecimal colBankShare = getShare(config.getCollectingBankShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage());
                    items.add(colBankShare.toPlainString());
                    BigDecimal aggrShare = getShare(config.getAggregatorShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage());
                    items.add(aggrShare.toPlainString());
                    BigDecimal nibssShare = getShare(config.getNibssShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage());
                    items.add(nibssShare.toPlainString());
                    BigDecimal billerBankShare = getShare(config.getBillerBankShare(),
                            t.getBaseTransaction().getTransactionFee(), config.isPercentage());
                    items.add(billerBankShare.toPlainString());
                    items.add( DATE_FORMAT_THREAD_LOCAL.get().format(t.getBaseTransaction().getTransactionDate()));
                    printer.printRecord(items);

                }
            }

        } catch (IOException e) {
            throw new RuntimeException("could nt gen. txn details file", e);
        }

        return writer.toString();
    }

    private void doCollectingBankShare(EbillspayTransaction t, EbillsBillingConfiguration config, Map<BankAccount, BigDecimal> map) {
        if (Utility.isEmptyBigDecimal(config.getCollectingBankShare()))
            return;

        String bankCode = t.getBaseTransaction().getSourceBank().getCode();
        Bank bank = bankRepository.findByCode(bankCode);
        if (null == bank)
            return;
        BankAccount acct = null;
        try {
            acct = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.EBILLSPAY);
        } catch (Exception e) {
            log.error("could not get acct for col. bank. {}", acct, e);
        }
        if (null != acct) {
            BigDecimal share = config.getCollectingBankShare();
            if (config.isPercentage())
                share = config.getCollectingBankShare().multiply(t.getBaseTransaction().getTransactionFee());

            share = share.setScale(2, ROUNDING_MODE);
            if (map.containsKey(acct))
                map.put(acct, map.get(acct).add(share));
            else
                map.put(acct, share);
        } else
            log.warn("could not get collecting bank acct: {}",bank.getName());

    }

    private void doAggregatorShare(EbillspayTransaction t, EbillsBillingConfiguration config, Map<BankAccount, BigDecimal> map) {
        if (Utility.isEmptyBigDecimal(config.getAggregatorShare()))
            return;

        List<Organization> aggrs = organizationRepository.findAggregatorForEbillsPayBiller(t.getBaseTransaction().getBiller().getId());
        if (null == aggrs || aggrs.isEmpty()) {
            log.warn("could not get aggregator for biller {}", t.getBaseTransaction().getBiller().getName());
            return;
        }

        Organization aggr = aggrs.get(0);
        aggrMap.put(t.getBaseTransaction().getBiller().getId(), aggr.getName());

        BankAccount acct = null;
        try {
            acct = bankAccountRepository.findByOrganizationAndProductCode(aggr.getId(), Product.EBILLSPAY);
        } catch (Exception e) {
            log.error("could not get acct for aggr. {}", acct, e);
        }
        if (null == acct) {
            log.warn("could not get acct for aggregator {}", aggr.getName());
            return;
        }


        BigDecimal share = config.getAggregatorShare();
        if (config.isPercentage())
            share = config.getAggregatorShare().multiply(t.getBaseTransaction().getTransactionFee());

        share = share.setScale(2, BillingProvider.ROUNDING_MODE);
        if (map.containsKey(acct))
            map.put(acct, map.get(acct).add(share));
        else
            map.put(acct, share);
    }

    private void doBillerBankShare(EbillspayTransaction t, EbillsBillingConfiguration config, Map<BankAccount, BigDecimal> map) {
        if (Utility.isEmptyBigDecimal(config.getBillerBankShare()) || null == config.getBillerBankCode())
            return;

        Bank bank = bankRepository.findByCode(config.getBillerBankCode());
        if (null == bank)
            return;

        billerBankMap.put(t.getBaseTransaction().getBiller().getId(), bank.getName());
        BankAccount acct = null;
        try {
            acct = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.EBILLSPAY);
        } catch (Exception e) {
            log.error("could not get acct for biller bank. {}", acct, e);
        }

        if (null != acct) {
            BigDecimal share = config.getBillerBankShare();
            if (config.isPercentage())
                share = config.getBillerBankShare().multiply(t.getBaseTransaction().getTransactionFee());

            share = share.setScale(2, BillingProvider.ROUNDING_MODE);
            if (map.containsKey(acct))
                map.put(acct, map.get(acct).add(share));
            else
                map.put(acct, share);
        } else
            log.warn("could not get biller bank acct: {}", bank.getName());
    }


}

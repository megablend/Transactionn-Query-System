package com.nibss.tqs.centralpay.billing;

import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
import com.nibss.tqs.centralpay.dto.CpayAccountSharingConfig;
import com.nibss.tqs.centralpay.dto.CpayMerchant;
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
 * Created by eoriarewo on 9/14/2016.
 */
@Component("cpayBillingProvider")
@Slf4j
@Transactional
public class CpayAccountBillingProvider implements BillingProvider {

    @Autowired
    private BillingHelper billingHelper;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ApplicationSettings appSettings;
    
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CpayAccountBillingReportHelper billingReportHelper;


    private Map<String, String> aggrMap = new HashMap<>();
    private Map<String,String> billerBankMap = new HashMap<>();

    private final static  String HEADERS[] = {
            "TRANSACTION ID","MERCHANT","SOURCE BANK","AGGREGATOR","BILLER BANK","FEE","NIBSS SHARE","COL. BANK SHARE",
            "AGGR. SHARE","BILLER BANK SHARE", "TRANSACTION DATE"
    };

    @Override
    @Transactional
    public Path getBillingZipFile(List<? extends Serializable> transactions, BillingPeriod billingPeriod) throws RuntimeException, IOException {
        List<AccountTransaction> trxns = transactions.stream().map( t -> (AccountTransaction)t).collect(Collectors.toList());

        //grp txns by collecting bank
        Map<String, List<AccountTransaction>> byColBank = trxns.stream().collect(Collectors.groupingBy(t -> t.getSourceBank().getCbnCode()));

        Map<String,BigDecimal> smartDetMap = new HashMap<>();
        byColBank.forEach( (k,v) -> {
            BigDecimal total = v.stream().map( t -> t.getFee()).reduce( (a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
            smartDetMap.put(k,total);
        });

        String smartDetFile = billingHelper.getSmartDetFile(smartDetMap, appSettings.cpaySmartDetCode());

        //group trxns by merchant
        Map<CpayMerchant, List<AccountTransaction>> byMerchant = trxns.stream().collect(Collectors.groupingBy( t -> t.getMerchant()));
        Map<BankAccount,BigDecimal> sharedSums = new HashMap<>();

        byMerchant.forEach( (k,v) -> {
            CpayAccountSharingConfig config = k.getSharingConfig();
            v.forEach( t -> {
                doAggregatorShare(t,config,sharedSums);
                doBillerBankShare(t,config,sharedSums);
                doCollectingBankShare(t,config,sharedSums);
            });
        });

        String paymentFile = billingHelper.getNIBSSPayPaymentFile(sharedSums,appSettings.cpayNarration(),appSettings.cpayPayerName());

        billingHelper.createPaymentProductFolder(Product.CENTRALPAY);

        DateFormat dtFormat = new SimpleDateFormat("ddMMyyyyHHmmss",Locale.ENGLISH);

        String strDate = dtFormat.format(new Date());
        String zipFileName = String.format("CentralPayAccountBilling_%s_%s.zip",strDate,billingPeriod);

        Path cpayBillingPath = Paths.get(appSettings.billingPaymentFolder(),Product.CENTRALPAY, zipFileName);

        Map<String,String> filesMap = new HashMap<>();


        String smarDetFileName = String.format("CpayAccountSmartDet_%s.txt",strDate);
        String nibssPaymentFileName = String.format("CpayAccountPaymentFile_%s.txt",strDate);
        String transactionDetailFile = String.format("TransactionDetails_%s.csv", strDate);

        filesMap.put(smarDetFileName,smartDetFile);
        filesMap.put(nibssPaymentFileName,paymentFile);
        filesMap.put(transactionDetailFile, buildTransactionDetails(byMerchant));

        //write zip file to disc
        billingHelper.writePaymentZipFile(filesMap, cpayBillingPath);
        log.trace("done generating zip payment file.");

        return cpayBillingPath;
    }

    @Override
    @Transactional
    public void generatePartyReports(List<? extends Serializable> transactions, BillingPeriod billingPeriod) {
        List<AccountTransaction> trxns = transactions.stream().map( t -> (AccountTransaction)t).collect(Collectors.toList());
        try {
            billingReportHelper.generateReports(trxns);
        } catch(Exception e) {
            log.error("could not generate billing report",e);
        }

    }


    private String buildTransactionDetails(Map<CpayMerchant,List<AccountTransaction>> txnMap) {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.EXCEL;
        try(CSVPrinter printer = new CSVPrinter(writer,format)) {

            printer.printRecord(HEADERS);

            for( CpayMerchant  k : txnMap.keySet()) {

                CpayAccountSharingConfig config = k.getSharingConfig();
                List<AccountTransaction> txns = txnMap.get(k);
                for(AccountTransaction t : txns) {
                    List<String> items = new ArrayList<>();
                    items.add(t.getSourceSessionId());
                    items.add(k.getName());
                    items.add(t.getSourceBank().getName());
                    items.add( aggrMap.get(k.getMerchantCode()) == null ? "" : aggrMap.get(k.getMerchantCode()));
                    items.add(billerBankMap.get(k.getMerchantCode()) == null ? "" : billerBankMap.get(k.getMerchantCode()));
                    items.add( t.getFee().toPlainString());
                    BigDecimal nibssShare = getFee(config.getNibssShare(),t.getFee(), config.isPercentage());
                    items.add(nibssShare.toPlainString());

                    BigDecimal colBankShare = getFee(config.getCollectingBankShare(),t.getFee(), config.isPercentage());
                    items.add(colBankShare.toPlainString());

                    BigDecimal aggrShare = getFee(config.getAggregatorShare(),t.getFee(),config.isPercentage());
                    items.add(aggrShare.toPlainString());

                    BigDecimal billerBankShare = getFee(config.getBillerBankShare(), t.getFee(), config.isPercentage());
                    items.add(billerBankShare.toPlainString());

                    items.add(DATE_FORMAT_THREAD_LOCAL.get().format(t.getTransactionDate()));

                    printer.printRecord(items);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    private void doCollectingBankShare(AccountTransaction t, CpayAccountSharingConfig config, Map<BankAccount, BigDecimal> map) {
        if(Utility.isEmptyBigDecimal(config.getCollectingBankShare()))
            return;

        Bank bank = bankRepository.findByCode(t.getSourceBank().getCbnCode());
        if( null != bank ) {
            
            BankAccount acct = null;
            try {
                acct = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.CENTRALPAY);
            } catch(Exception e) {
                log.error("could not get acct for col. bank. {}",e);
            }
            if( acct != null) {
                BigDecimal share = config.getCollectingBankShare();
                if( config.isPercentage())
                    share = config.getCollectingBankShare().multiply(t.getFee());

                share = share.setScale(2,ROUNDING_MODE);
                if(map.containsKey(acct))
                    map.put(acct,map.get(acct).add(share));
                else
                    map.put(acct,share);
            } else 
                log.warn("Could not get acct details for {}  with id {} for product {}",bank,bank.getId(),Product.CENTRALPAY);
        } else 
            log.trace("Could not get bank with CBN Code {}",t.getSourceBank().getCbnCode());
    }

    private void doAggregatorShare( AccountTransaction t,CpayAccountSharingConfig config,Map<BankAccount,BigDecimal> map) {
        if(Utility.isEmptyBigDecimal(config.getAggregatorShare()))
            return;

        List<Organization> orgs = organizationRepository.findAggregatorForCentralPayMerchant(t.getMerchant().getMerchantCode());
        if( null == orgs || orgs.isEmpty())
            return;
        Organization org = orgs.get(0);
        if( null != org) {
            aggrMap.put(t.getMerchant().getMerchantCode(),org.getName());

            BankAccount acct = null;
            try {
                acct = bankAccountRepository.findByOrganizationAndProductCode(org.getId(), Product.CENTRALPAY);
            } catch(Exception e) {
                log.error("could not get bank acct for aggr. {}", org, e);
            }
            if( null != acct) {
                BigDecimal share = config.getAggregatorShare();
                if( config.isPercentage())
                    share = config.getAggregatorShare().multiply(t.getFee());

                share = share.setScale(2, BillingProvider.ROUNDING_MODE);
                if( map.containsKey(acct))
                    map.put(acct,map.get(acct).add(share));
                else
                    map.put(acct,share);
            } else
                log.warn("could not get acct details for aggregator {}", org.getName());
        }
    }

    private  void doBillerBankShare(AccountTransaction t, CpayAccountSharingConfig config,Map<BankAccount,BigDecimal> map) {
        if(Utility.isEmptyBigDecimal(config.getBillerBankShare()) || null == config.getBillerBankCode())
            return;

        Bank bank = bankRepository.findByCode(config.getBillerBankCode());
        
        if( null != bank) {
            billerBankMap.put(t.getMerchant().getMerchantCode(), bank.getName());
            BankAccount acct = null;
            try {
                acct = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.CENTRALPAY);
            } catch(Exception e) {
                log.error("could not get account for bank {}",bank,e);
            }
            
            if( acct != null) {
                BigDecimal share = config.getBillerBankShare();
                if( config.isPercentage())
                    share = config.getBillerBankShare().multiply(t.getFee());

                share = share.setScale(2, BillingProvider.ROUNDING_MODE);
                if(map.containsKey(acct))
                    map.put(acct,map.get(acct).add(share));
                else
                    map.put(acct,share);
            } else
                log.warn("no acct maintained for {}", bank.getName());
        }
    }

    private  BigDecimal getFee(BigDecimal amount, BigDecimal fee, boolean percentage) {
        if(Utility.isEmptyBigDecimal(amount))
            return BigDecimal.ZERO;

        BigDecimal share = amount;
        if(percentage)
            share = amount.multiply(fee);
        return share.setScale(2,ROUNDING_MODE);
    }

    @Override
    public void cleanUp() {
        aggrMap.clear();
        billerBankMap.clear();
    }
}

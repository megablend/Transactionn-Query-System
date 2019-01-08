package com.nibss.tqs.corporatelounge.billing;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.Bank;
import com.nibss.corporatelounge.dto.BillingSetting;
import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.BankAccount;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.repositories.BankAccountRepository;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.corporatelounge.repositories.CorporateLoungeBillingSettingRepository;
import com.nibss.tqs.util.BillingHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by eoriarewo on 10/16/2017.
 */
@Component("clAnnualBillingProvider")
@Slf4j
public class CLoungeAnnualBillingProvider implements BillingProvider {

    @Autowired
    private BillingHelper billingHelper;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private CorporateLoungeBillingSettingRepository settingRepository;

    @Autowired
    private CorporateLoungeBillingReportHelper billingReportHelper;


    private static final String[] HEADERS = {"CLIENT", "ACCOUNT NUMBER", "ACCOUNT NAME", "BANK", "BANK SHARE","NIBSS SHARE"};



    @Override
    public Path getBillingZipFile(List<? extends Serializable> transactions, BillingPeriod billingPeriod) throws RuntimeException, IOException {
        List<Account> accounts = transactions.stream().map(a -> (Account)a).collect(toList());


        BillingSetting setting = null;
        try {
            setting = settingRepository.findFirstByOrderById();
        } catch (Exception e) {
            log.error("could not get billing setting", e);
        }
        if (null == setting) {
            log.warn("sharing config not yet maintained for clounge");
            return null;
        }

        //group by bank
        Map<Bank, List<Account>> byBank = accounts.stream().collect(groupingBy( a -> a.getBank()));

        //for each bank, get bank account for product.
        //after getting acct, bank is due annualFee*lst.size*bankShare

        Map<BankAccount, BigDecimal> shareMap = new HashMap<>();

        BillingSetting finalSetting = setting;
        byBank.forEach( (k, v) -> {
            BankAccount ba = null;
            try {
                com.nibss.tqs.core.entities.Bank bank = bankRepository.findByCode(k.getCbnCode());
                if( null != bank)
                    ba = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.CORPORATE_LOUNGE);
                else
                    log.warn("bank with code {} does not exist", k.getCbnCode());
            } catch (Exception e) {
                log.error("could not get clounge acct for bank {}", k.getCbnCode(),e);
            }

            if( null != ba) {
                BigDecimal share = finalSetting.getAnnualFee().multiply(finalSetting.getBankShare())
                        .multiply(new BigDecimal(v.size())).setScale(2, ROUNDING_MODE);
                shareMap.put(ba,share);
            } else
                log.warn("bank account not maintained for bank {}", k.getCbnCode());
        });

        String nibssPayFile = billingHelper.getNIBSSPayPaymentFile(shareMap,
                "Corporate Lounge Annual Subscription Commission", "NIBSS Plc.");
        billingHelper.createPaymentProductFolder(Product.CLOUNGE_ANNUAL);


        DateFormat dtFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH);

        String strDate = dtFormat.format(new Date());
        String zipFileName = String.format("CLoungeAnnualBilling_%s_%s.zip", strDate,billingPeriod);


        Path customBillingFilePath = Paths.get(appSettings.billingPaymentFolder(), Product.CLOUNGE_ANNUAL, zipFileName);

        Map<String, String> filesMap = new HashMap<>();

        String nibssPaymentFileName = String.format("CLoungeAnnualBillingNIBSSPaymentFile_%s.txt", strDate);
        String transactionDetailFile = String.format("TransactionDetails_%s.csv", strDate);

        filesMap.put(nibssPaymentFileName, nibssPayFile);
        filesMap.put(transactionDetailFile, buildTransactionDetails(accounts,setting));

        billingHelper.writePaymentZipFile(filesMap, customBillingFilePath);

        return customBillingFilePath;


    }

    @Override
    public void generatePartyReports(List<? extends Serializable> transactions, BillingPeriod billingPeriod) {
        try {
            BillingSetting setting =  settingRepository.findFirstByOrderById();
            if( null == setting) {
                log.warn("billing setting not maintained for Corporate Lounge");
                return;
            }

            List<Account> accounts = transactions.stream().map( a -> (Account)a).collect(toList());
            billingReportHelper.generateAnnualBillingReport(accounts, setting);
        } catch (Exception e) {
            log.error("could not generate billing reports",e);
        }
    }

    @Override
    public void cleanUp() {

    }

    private String buildTransactionDetails(List<Account> accounts, BillingSetting setting) {
        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            printer.printRecord(HEADERS);

            for (Account b : accounts) {
                List<String> items = new ArrayList<>();
                items.add(b.getOrganization().getName());
                items.add(b.getAccountNumber());
                items.add(b.getAccountName());
                items.add(b.getBank().getName());
                BigDecimal bankShare = setting.getBankShare().multiply(setting.getAnnualFee())
                        .setScale(2, ROUNDING_MODE);
                items.add(bankShare.toPlainString());

                BigDecimal nibssShare = setting.getNibssShare().multiply(setting.getAnnualFee())
                        .setScale(2, ROUNDING_MODE);
                items.add(nibssShare.toPlainString());

                printer.printRecord(items);
            }
        } catch (IOException e) {
            throw new RuntimeException("could not generate detail report", e);
        }
        return writer.toString();
    }

}

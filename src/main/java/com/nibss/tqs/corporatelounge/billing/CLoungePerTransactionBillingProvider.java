package com.nibss.tqs.corporatelounge.billing;

import com.nibss.corporatelounge.dto.AccountBalance;
import com.nibss.corporatelounge.dto.Bank;
import com.nibss.corporatelounge.dto.BillingSetting;
import com.nibss.corporatelounge.dto.Organization;
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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by eoriarewo on 10/11/2017.
 */
@Component("clBillingPerTransactionProvider")
@Slf4j
public class CLoungePerTransactionBillingProvider implements BillingProvider {

    @Autowired
    private BillingHelper billingHelper;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private CorporateLoungeBillingReportHelper billingReportHelper;

    private static final String[] HEADERS = {"SESSION ID", "CLIENT", "ACCOUNT NUMBER", "ACCOUNT NAME", "BANK",  "BANK SHARE","NIBSS SHARE"};

    @Autowired
    private CorporateLoungeBillingSettingRepository settingRepository;

    @Override
    public Path getBillingZipFile(List<? extends Serializable> transactions, BillingPeriod billingPeriod) throws RuntimeException, IOException {

        /*
        for each accountBalance, get the bank n the organization
        for group the balances by organization, then generate debit files for each one
        group balances by bank then generate credit for each bank
         */
        List<AccountBalance> balances = transactions.stream().map(t -> (AccountBalance) t).collect(Collectors.toList());

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

        Map<Organization, List<AccountBalance>> byOrganization = balances.stream().collect(groupingBy(AccountBalance::getOrganization));
        //for each organization, generate debit file entry. The amount per org is lst.size() * perTransactionFee

        Map<BankAccount, BigDecimal> debitMap = new HashMap<>();

        BillingSetting finalSetting = setting;
        byOrganization.forEach((k, v) -> {
            BankAccount ba = new BankAccount(k.getDebitAccountName(), k.getDebitAccountNumber(), k.getDebitBankCode());
            BigDecimal debitAmt = finalSetting.getPerTransactionFee().multiply( new BigDecimal(v.size()));
            debitMap.put(ba,debitAmt);
        });

        String debitContent = billingHelper.getHAWKDebitFile(debitMap,"Corporate Lounge Subscription Debit");


        Map<Bank, List<AccountBalance>> byBank = balances.stream().collect(groupingBy(a -> a.getAccount().getBank()));
        //for each bank, generate credit file entry. entry for each bank is lst.size() * perTransactionFee * bankShare

        Map<BankAccount, BigDecimal> creditMap = new HashMap<>();
        byBank.forEach( (k, v) -> {
            BankAccount ba = null;
            com.nibss.tqs.core.entities.Bank bank = bankRepository.findByCode(k.getCbnCode());
            if( null  != bank)
                ba = bankAccountRepository.findByOrganizationAndProductCode(bank.getId(), Product.CORPORATE_LOUNGE);
            else
                log.warn("bank with code {} does not exists", k.getCbnCode());

            if( null == ba)
                log.warn("no Corporate Lounge Acct Profiled for bank {}", k.getCbnCode());
            else {
                BigDecimal credit = finalSetting.getPerTransactionFee().multiply(finalSetting.getBankShare())
                        .multiply(new BigDecimal(v.size()));

                creditMap.put(ba, credit);
            }
        });

        String nibssPayFile = billingHelper.getNIBSSPayPaymentFile(creditMap,
                "C.Lounge Per-Transaction Subscription Commission", "NIBSS Plc.");
        billingHelper.createPaymentProductFolder(Product.CLOUNGE_PER_TRANSACTION);


        DateFormat dtFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.ENGLISH);

        String strDate = dtFormat.format(new Date());
        String zipFileName = String.format("CLoungePerTransactionBilling_%s_%s.zip", strDate,billingPeriod);


        Path customBillingFilePath = Paths.get(appSettings.billingPaymentFolder(), Product.CLOUNGE_PER_TRANSACTION, zipFileName);

        Map<String, String> filesMap = new HashMap<>();

        String nibssPaymentFileName = String.format("CLoungePerTransactionBillingNIBSSPaymentFile_%s.txt", strDate);
        String transactionDetailFile = String.format("TransactionDetails_%s.csv", strDate);

        String hawkDebitFileName = String.format("CLoungePerTransactionDebit_%s.csv",strDate);

        filesMap.put(nibssPaymentFileName, nibssPayFile);
        filesMap.put(hawkDebitFileName, debitContent);
        filesMap.put(transactionDetailFile, buildTransactionDetails(balances,setting));

        billingHelper.writePaymentZipFile(filesMap, customBillingFilePath);

        return customBillingFilePath;
    }

    @Override
    public void generatePartyReports(List<? extends Serializable> transactions, BillingPeriod billingPeriod) {
        List<AccountBalance> balances = transactions.stream().map( t -> (AccountBalance)t).collect(Collectors.toList());
        try {
            BillingSetting setting = settingRepository.findFirstByOrderById();
            if( null == setting) {
                log.warn("billing setting not maintained for Corporate Lounge");
                return;
            }

            billingReportHelper.generatePerTransactionBillingReport(balances, setting);
        } catch (Exception e) {
            log.error("could not generate per-transaction billing report",e);
        }
    }

    @Override
    public void cleanUp() {

    }

    private String buildTransactionDetails(List<AccountBalance> balances, BillingSetting setting) {
        StringWriter writer = new StringWriter();

        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            printer.printRecord(HEADERS);

            for (AccountBalance b : balances) {
                List<String> items = new ArrayList<>();
                items.add(b.getSessionId());
                items.add(b.getOrganization().getName());
                items.add(b.getAccount().getAccountNumber());
                items.add(b.getAccount().getAccountName());
                items.add(b.getAccount().getBank().getName());
                BigDecimal bankShare = setting.getBankShare().multiply(setting.getPerTransactionFee())
                        .setScale(2, ROUNDING_MODE);
                items.add(bankShare.toPlainString());

                BigDecimal nibssShare = setting.getNibssShare().multiply(setting.getPerTransactionFee())
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

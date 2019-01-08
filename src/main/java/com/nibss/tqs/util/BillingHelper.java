package com.nibss.tqs.util;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.BankAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by eoriarewo on 8/18/2016.
 */
@Component
@Scope("prototype")
@Slf4j
public class BillingHelper {

    public final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_DOWN;

    private final static BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public static final String[] DEBIT_HEADER = {"serialNumber","beneficiary","amount","accountNumber","sortCode","description"};

    @Autowired
    private ApplicationSettings appSettings;

    public String getSmartDetFile(Map<String,BigDecimal> debitMap, String productSmartDetCode) throws RuntimeException {

        String datePart = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).format(new Date());
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        try(CSVPrinter printer = new CSVPrinter(writer,format)) {
            for( String bankCode: debitMap.keySet()) {
                List<String> items = new ArrayList<>();
                items.add(String.format("%s%s",bankCode,appSettings.ebillspaySortCodeSuffix()));
                items.add(productSmartDetCode);
                items.add(datePart);
                BigDecimal value = debitMap.get(bankCode).setScale(2,ROUNDING_MODE);
                items.add(value.toPlainString());

                printer.printRecord(items);
            }

            return  writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("could not generate smartdet",e);
        }
    }

    public String getHAWKDebitFile(final Map<BankAccount,BigDecimal> debitMap, String narration) throws RuntimeException {
        //serialNumber,beneficiary,amount,accountNumber,sortCode,description

        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT;
        try(CSVPrinter printer = new CSVPrinter(writer,format)) {
            LongAdder inc = new LongAdder();
            inc.increment();

            printer.printRecord(Arrays.asList(DEBIT_HEADER));

            for( BankAccount acct : debitMap.keySet()) {
                List<String> items = new ArrayList<>();
                items.add(Integer.toString(inc.intValue()));
                items.add(acct.getAccountName() == null ? "" : acct.getAccountName());
                items.add(debitMap.get(acct).toPlainString());
                items.add(acct.getAccountNumber());
                items.add(acct.getBankCode());
                items.add(narration);

                printer.printRecord(items);
                inc.increment();

            }


        } catch (IOException e) {
            throw new RuntimeException("could not generate debit file",e);
        }

        return writer.toString();
    }

    public String getNIBSSPayPaymentFile(Map<BankAccount,BigDecimal> paymentMap, String narration, String payerName) throws RuntimeException {
        /*
        format is
        1. serialNo
        2. account no
        3. bank sort code
        4. amount in kobo
        5. acct name
        6. narration
        7. payer name
         */

        StringWriter writer = new StringWriter();

        CSVFormat format = CSVFormat.DEFAULT;

        try(CSVPrinter printer = new CSVPrinter(writer,format)) {
            LongAdder adder = new LongAdder();
            adder.increment();
            for(BankAccount acct: paymentMap.keySet()) {
                log.trace("Account entry: {}, {}, {}",acct.getAccountName(), acct.getAccountNumber(), acct.getBankCode());
                List<String> items = new ArrayList<>();
                items.add(String.valueOf(adder.intValue()));
                items.add(acct.getAccountNumber());
                items.add( String.format("%s%s",acct.getBankCode(), appSettings.ebillspaySortCodeSuffix()));
                BigDecimal amountInKobo = paymentMap.get(acct).multiply(ONE_HUNDRED).setScale(0,ROUNDING_MODE);
                items.add(amountInKobo.toPlainString());
                items.add(acct.getAccountName());
                items.add(narration);
                items.add(payerName);
                printer.printRecord(items);
                adder.increment();
            }

            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("could not generate payment file", e);
        }

    }

    public  void writePaymentZipFile(Map<String,String> fileMap, Path zipLocation) throws IOException {
        try(ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipLocation, StandardOpenOption.CREATE)))) {
            for(String key : fileMap.keySet()) {
                String content = fileMap.get(key);
                if( key == null || content == null) {
                    log.warn("Null entry in map found for key {}",key);
                    continue;
                }

                ZipEntry entry = new ZipEntry(key);
                outputStream.putNextEntry(entry);
                outputStream.write(content.getBytes());
                outputStream.closeEntry();
            }
        }
    }

    public void createPaymentProductFolder(String productFolder) throws RuntimeException {
        Path path = Paths.get(appSettings.billingPaymentFolder(), productFolder);
        if( !Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new RuntimeException("could not create base folder path",e);
            }
        }
    }

    public void createClientReportFolder(String product, String clientCode) throws RuntimeException {

        Path path = Paths.get(appSettings.billingReportFolder(),product,clientCode);
        if( !Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new RuntimeException("could not create client report folder",e);
            }
        }
    }

    public void createProductReportFolder(String productFolder) throws RuntimeException {
        Path path = Paths.get(appSettings.billingReportFolder(), productFolder);
        if( !Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                throw new RuntimeException("could not create base report folder path",e);
            }
        }
    }


}

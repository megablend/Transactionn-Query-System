package com.nibss.tqs.report;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.ajax.*;
import com.nibss.tqs.config.ApplicationSettings;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 9/9/2016.
 */
@Component
@Scope("prototype")
public class AggregatorTransactionReportDownloader extends AbstractReportDownloader {

    @Autowired
    private ApplicationSettings appSettings;

    @Override
    protected ApplicationSettings getAppSettings() {
        return appSettings;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateEbillspayReport(List<AjaxEbillsPayTransaction> transactions, DownloadType downloadType) throws Exception {

        List<TransactionReport> items = getEbillsTransactions(transactions);
        /*

         Arrays.asList("Transaction ID",
                "Biller Name",
                "Product",
                "Source Bank",
                "Destination Bank",
                "Customer ID",
                "Transaction Date",
                "Date Approved",
                "Amount (N)",
                "Status",
                "");
         */

        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID", TransactionReport.SESSION_ID);
        TextColumnBuilder<String> merchCol = getTextColumn("Biller", TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> productCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder<String> sourceBankCol = getTextColumn("Source Bank", TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> destBankCol = getTextColumn("Destination Bank", TransactionReport.DESTINATION_BANK);
        TextColumnBuilder<String> custIdCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<Date> trxnDateCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<Date> dtAppCol = getDateColumn("Date Approved", TransactionReport.DATE_APPROVED);
        TextColumnBuilder<BigDecimal> amountCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<String> statusCol = getTextColumn("Status", TransactionReport.STATUS);


        TextColumnBuilder<? extends Serializable>[] columns =new TextColumnBuilder[] {
                sessIdCol,merchCol,productCol,sourceBankCol,destBankCol,custIdCol,trxnDateCol,dtAppCol,amountCol,statusCol
        };

        return generateReport(items,downloadType,EBILLSPAY, columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateCpayCardReport(List<AjaxCpayCardTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getCardTransactions(transactions);
        /*
         Arrays.asList("MERCHANT",
                "GATEWAY",
                "CPAY REF",
                "MERCHANT REF",
                "PRODUCT",
                "AMOUNT (N)",
                "TRXN DATE",
                "STATUS");
         */

        TextColumnBuilder<String> merCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> gatewayCol = getTextColumn("Gateway",TransactionReport.GATEWAY);
        TextColumnBuilder<String> cpayRefCol = getTextColumn("CPAY Ref.",TransactionReport.GATEWAY);
        TextColumnBuilder<String> merchRefCol = getTextColumn("Merchant Ref.",TransactionReport.MERCHANT_REF);
        TextColumnBuilder<String> prodCol = getTextColumn("Product",TransactionReport.PRODUCT);
        TextColumnBuilder<String> custIdCol = getTextColumn("Customer ID",TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<Date> trxnDateCol = getDateColumn("Transaction Date",TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[] {
                merCol,gatewayCol,cpayRefCol,merchRefCol,prodCol,custIdCol,amtCol,trxnDateCol,statCol
        };

        return generateReport(items,downloadType,CPAYCARD,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateCpayAccountReport(List<AjaxCpayAccountTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getAccountTransactions(transactions);
        /*
         Arrays.asList(
                "MERCHANT",
                "SESSION ID",
                "CPAY REF",
                "MERCHANT_TXN_REF",
                "SOURCE BANK",
                "PAYMENT TYPE",
                "PRODUCT",
                "AMOUNT (N)",
                "TRANSACTION DATE",
                "Status");
         */

        TextColumnBuilder<String> merCol = getTextColumn("Merchant", TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID", TransactionReport.SESSION_ID);
        TextColumnBuilder<String> cpayRefCol = getTextColumn("CPAY Ref.", TransactionReport.CPAY_REF);
        TextColumnBuilder<String> merchRefCol = getTextColumn("Merchant Ref.", TransactionReport.MERCHANT_REF);
        TextColumnBuilder<String> srcBankCol = getTextColumn("Source Bank", TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> payTypeCol = getTextColumn("Payment Type", TransactionReport.PAYMENT_TYPE);
        TextColumnBuilder<String> customerCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<String> prodCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<Date> txnDateCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statusCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[] {
                merCol,sessIdCol,cpayRefCol,merchRefCol,srcBankCol,payTypeCol,customerCol,prodCol,amtCol,txnDateCol,statusCol
        };

        return generateReport(items,downloadType,CPAYACCOUNT,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateMerchantPayReport(List<AjaxMcashTransaction> transactions, DownloadType downloadType) throws Exception {
        /*
        Arrays.asList("MERCHANT",
                "TELCO", "USSD AGGR.",
                "SOURCE BANK", "SESSION ID", "PAYMENT REF.", "AMOUNT (N)",
                "TRANSACTION DATE", "STATUS");
         */
        List<TransactionReport> items = getMerchantPayTransactions(transactions);

        TextColumnBuilder<String> merCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> merCodeCol = getTextColumn("Merchant Code",TransactionReport.MERCHANT_CODE);
        TextColumnBuilder<String> telCol = getTextColumn("Telco",TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggCol = getTextColumn("USSD Aggr.",TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<String> srcBankCol = getTextColumn("Source Bank",TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> refCodeCol = getTextColumn("Reference Code", TransactionReport.REFERENCE_CODE);
        TextColumnBuilder<String> payRefCol = getTextColumn("Payment Ref.",TransactionReport.PAYMENT_REF);
        TextColumnBuilder<String> phoneNoCol = getTextColumn("Phone No.",TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<Date> dateCol = getDateColumn("Transaction Date",TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statCol = getTextColumn("Debit Status",TransactionReport.STATUS);
//        TextColumnBuilder<String> credStatCol = getTextColumn("Credit Status",TransactionReport.CREDIT_STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[] {
                merCol,merCodeCol,telCol,ussdAggCol,srcBankCol,sessIdCol,refCodeCol,payRefCol,phoneNoCol,amtCol,dateCol,statCol
//                ,credStatCol
        };

        return generateReport(items,downloadType,USSD_MERCHANT_PAYMENT,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateUssdBillPaymentReport(List<AjaxUssdTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getUssdTransactions(transactions);
        /*
         Arrays.asList("MERCHANT",
                "SESSION ID",
                "TRANSACTION REFERENCE",
                "TELCO",
                "USSD AGGR.",
                "SOURCE BANK CODE",
                "AMOUNT (N)",
                "REQUEST TIME",
                "SOURCE STATUS",
                "DESTINATION STATUS"
        );
         */

        TextColumnBuilder<String> merCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> phoneNoCol = getTextColumn("Phone Number",TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<String> telCol = getTextColumn("Telco",TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggCol = getTextColumn("USSD Aggr.",TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<String> srcBankCodeCol = getTextColumn("Source Bank Code",TransactionReport.SOURCE_BANK_CODE);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<Date> trxnDate = getDateColumn("Request Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> srcStatus = getTextColumn("Source Status",TransactionReport.SOURCE_RESPONSE_DESCRIPTION);
        TextColumnBuilder<String> destStatus = getTextColumn("Destination Status",TransactionReport.DESTINATION_RESPONSE_DESCRIPTION);


        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[] {
                merCol,sessIdCol,phoneNoCol,telCol,ussdAggCol,srcBankCodeCol,amtCol,trxnDate,srcStatus,destStatus
        };

        return generateReport(items,downloadType,USSD_BILL_PAYMENT,columns);
    }

    @Override
    public ByteArrayOutputStream generateMerchantList(List<Merchant> merchants, DownloadType downloadType) throws Exception {
        return null;
    }
}

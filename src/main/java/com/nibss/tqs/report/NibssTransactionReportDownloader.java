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
import java.util.stream.Collectors;


/**
 * Created by eoriarewo on 9/8/2016.
 */
@Component
@Scope("prototype")
public class NibssTransactionReportDownloader extends AbstractReportDownloader {

    @Autowired
    private  ApplicationSettings appSettings;

   /* @Override
    @Transactional
    public ByteArrayOutputStream generateEbillspayReport(List<BaseTransaction> transactions, DownloadType downloadType) throws Exception {

        List<TransactionReport> items = transactions.stream().map(t -> new TransactionReport(t)).collect(Collectors.toList());

        *//*
        return Arrays.asList("Transaction ID","Biller Name","Product","Source Bank","Destination Bank", "Customer ID",
                "Transaction Date","Date Approved","Amount (N)","Transaction Fee","Status","");
         *//*
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> billerNameCol = getTextColumn("Biller",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> productCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder<String> sourceBankCol = getTextColumn("Source Bank", TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> destBankCol = getTextColumn("Destination Bank", TransactionReport.DESTINATION_BANK);
        TextColumnBuilder<String> custIdCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<Date> trxnDateCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<Date> dtApprovedCol = getDateColumn("Date Approved",TransactionReport.DATE_APPROVED);
        TextColumnBuilder<BigDecimal> amountCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Transaction Fee", TransactionReport.FEE);
        TextColumnBuilder<String> statusCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[]{
                sessIdCol,billerNameCol,productCol,sourceBankCol,destBankCol,custIdCol,trxnDateCol,dtApprovedCol,amountCol,feeCol,statusCol
        };


        return generateReport(items,downloadType,EBILLSPAY,columns);
    }*/


    @Override
    public ByteArrayOutputStream generateEbillspayReport(List<AjaxEbillsPayTransaction> transactions, DownloadType downloadType) throws Exception {

        List<TransactionReport> items = transactions.stream().map(t -> new TransactionReport(t)).collect(Collectors.toList());

        /*
        return Arrays.asList("Transaction ID","Biller Name","Product","Source Bank","Destination Bank", "Customer ID",
                "Transaction Date","Date Approved","Amount (N)","Transaction Fee","Status","");
         */
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> billerNameCol = getTextColumn("Biller",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> productCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder<String> sourceBankCol = getTextColumn("Source Bank", TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> branchCodeCol = getTextColumn("Branch Code", TransactionReport.BRANCH_CODE);
        TextColumnBuilder<String> destBankCol = getTextColumn("Destination Bank", TransactionReport.DESTINATION_BANK);
        TextColumnBuilder<String> custIdCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<Date> trxnDateCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<Date> dtApprovedCol = getDateColumn("Date Approved",TransactionReport.DATE_APPROVED);
        TextColumnBuilder<BigDecimal> amountCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Transaction Fee", TransactionReport.FEE);
        TextColumnBuilder<String> statusCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[]{
                sessIdCol,billerNameCol,productCol,sourceBankCol,branchCodeCol,destBankCol,custIdCol,trxnDateCol,dtApprovedCol,amountCol,feeCol,statusCol
        };


        return generateReport(items,downloadType,EBILLSPAY,columns);
    }


    @Override
    @Transactional
    public ByteArrayOutputStream generateCpayCardReport(List<AjaxCpayCardTransaction> transactions, DownloadType downloadType) throws  Exception {
        List<TransactionReport> items = transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());

        /*
         return Arrays.asList("MERCHANT",
                "GATEWAY",
                "CPAY REF",
                "MERCHANT REF",
                "PRODUCT",
                "AMOUNT (N)",
                "TRXN DATE",
                "DATE APPROVED",
                "STATUS");
         */
        TextColumnBuilder<String> merchantCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> gatewayCol = getTextColumn("Gateway",TransactionReport.GATEWAY);
        TextColumnBuilder<String> cpayRefCol = getTextColumn("CPAY Ref",TransactionReport.CPAY_REF);
        TextColumnBuilder<String> mpayRefCol = getTextColumn("Merchant Ref.",TransactionReport.MERCHANT_REF);
        TextColumnBuilder<String> productCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder<String> custIdCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<BigDecimal> amountCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<Date> txnDateCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<Date> dtAppCol = getDateColumn("Date Approved",TransactionReport.DATE_APPROVED);
        TextColumnBuilder<String> statusCol = getTextColumn("Status",TransactionReport.STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[] {
                merchantCol,gatewayCol,cpayRefCol,mpayRefCol,productCol,custIdCol,amountCol,txnDateCol,dtAppCol,statusCol
        };

        return generateReport(items,downloadType, CPAYCARD,columns);

    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateCpayAccountReport(List<AjaxCpayAccountTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());

        /*
        return Arrays.asList(
                "MERCHANT",
                "SESSION ID",
                "CPAY REF",
                "MERCHANT_TXN_REF",
                "SOURCE BANK",
                "PAYMENT TYPE",
                "PRODUCT",
                "AMOUNT (N)",
                "FEE (N)",
                "TRANSACTION DATE",
                "Status");
         */

        TextColumnBuilder<String> merchantCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> cpayRefCol = getTextColumn("Cpay Ref",TransactionReport.CPAY_REF);
        TextColumnBuilder<String> merchantRefCol = getTextColumn("Merchant Ref",TransactionReport.MERCHANT_REF);
        TextColumnBuilder<String> sourceBankCol = getTextColumn("Source Bank",TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> payTypeCol = getTextColumn("Payment Type",TransactionReport.PAYMENT_TYPE);
        TextColumnBuilder<String> customerCol = getTextColumn("Customer ID",TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<String> productCol = getTextColumn("Product",TransactionReport.PRODUCT);
        TextColumnBuilder<BigDecimal> amountCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Fee (N)",TransactionReport.FEE);
        TextColumnBuilder<Date> trxnDateCol = getDateColumn("Transaction Date",TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statusCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[]{
                merchantCol,sessIdCol,cpayRefCol,merchantRefCol,sourceBankCol,payTypeCol,customerCol,productCol,amountCol,feeCol,trxnDateCol,statusCol
        };

        return generateReport(items,downloadType,CPAYACCOUNT,columns);

    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateMerchantPayReport(List<AjaxMcashTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());

        /*
         Arrays.asList("MERCHANT",
                "TELCO", "USSD AGGR.",
                "SOURCE BANK", "SESSION ID", "PAYMENT REF.", "AMOUNT (N)", "FEE (N)",
                "TRANSACTION DATE", "STATUS");
         */

        TextColumnBuilder<String> merchCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> merchCodeCol = getTextColumn("Merchant Code",TransactionReport.MERCHANT_CODE);
        TextColumnBuilder<String> telcoCol = getTextColumn("Telco",TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggCol = getTextColumn("USSD Aggregator",TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<String> sourceBankCol = getTextColumn("Source Bank",TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> destBankCol = getTextColumn("Dest. Bank",TransactionReport.DESTINATION_BANK);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> refCodeCol = getTextColumn("Reference Code", TransactionReport.REFERENCE_CODE);
        TextColumnBuilder<String> paymentRef = getTextColumn("Payment Ref.",TransactionReport.PAYMENT_REF);
        TextColumnBuilder<String> phoneNoCol = getTextColumn("Phone No.",TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<BigDecimal> amountCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Fee (N)",TransactionReport.FEE);
        TextColumnBuilder<Date> trxnDateCol = getDateColumn("Transaction Date",TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statusCol = getTextColumn("Debit Status",TransactionReport.STATUS);
        TextColumnBuilder<String> credStatusCol = getTextColumn("Credit Status",TransactionReport.CREDIT_STATUS);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[]{
          merchCol,merchCodeCol,telcoCol,ussdAggCol,sourceBankCol,destBankCol,sessIdCol,refCodeCol,paymentRef,phoneNoCol,amountCol,feeCol,trxnDateCol,statusCol,credStatusCol
        };

        return generateReport(items,downloadType,USSD_MERCHANT_PAYMENT,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateUssdBillPaymentReport(List<AjaxUssdTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());
        /*
         return Arrays.asList("MERCHANT",
                "SESSION ID",
                "TRANSACTION REFERENCE",
                "TELCO",
                "USSD AGGR.",
                "SOURCE BANK CODE",
                "AMOUNT (N)",
                "FEE (N)",
                "REQUEST TIME",
                "SOURCE RESPONSE",
                "DESTINATION RESPONSE"
               );
         */

        TextColumnBuilder<String> merchCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> transactionRef = getTextColumn("Transaction Ref.",TransactionReport.TRANSACTION_REF);
        TextColumnBuilder<String> phoneNoCol = getTextColumn("Phone Number",TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<String> telcoCol = getTextColumn("Telco",TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggCol = getTextColumn("USSD Aggregator",TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<String> srcBankCodeCol = getTextColumn("Source Bank Code",TransactionReport.SOURCE_BANK_CODE);
        TextColumnBuilder<BigDecimal> amountCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Fee (N)",TransactionReport.FEE);
        TextColumnBuilder<Date> trxnDateCol = getDateColumn("Transaction Date",TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> sourceResCol = getTextColumn("Source Status",TransactionReport.SOURCE_RESPONSE_DESCRIPTION);
        TextColumnBuilder<String> destResCol = getTextColumn("Destination Status",TransactionReport.DESTINATION_RESPONSE_DESCRIPTION);

        TextColumnBuilder<? extends Serializable>[] columns = new TextColumnBuilder[] {
          merchCol,sessIdCol,transactionRef,phoneNoCol,telcoCol,ussdAggCol,srcBankCodeCol,amountCol,feeCol,trxnDateCol,sourceResCol,destResCol
        };

        return generateReport(items,downloadType,USSD_BILL_PAYMENT,columns);
    }

    @Override
    public ByteArrayOutputStream generateMerchantList(List<Merchant> merchants, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = merchants.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());

        /*
         Arrays.asList("Merchant Code",
                "Name",
        "Email", "Phone Number", "LGA", "State", "Status", "Date Created");
         */
        TextColumnBuilder<String> merchantCodeCol = getTextColumn("Merchant Code", TransactionReport.MERCHANT_CODE);
        TextColumnBuilder<String> merchantNameCol = getTextColumn("Merchant", TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> emailCol = getTextColumn("Email", TransactionReport.EMAIL);
        TextColumnBuilder<String> phoneCol = getTextColumn("Phone", TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<String> accountNoCol = getTextColumn("Account Number", TransactionReport.ACCOUNT_NUMBER);
        TextColumnBuilder<String> acctNameCol = getTextColumn("Account Name", TransactionReport.ACCOUNT_NAME);
        TextColumnBuilder<String> bankCol = getTextColumn("Bank", TransactionReport.DESTINATION_BANK);
        TextColumnBuilder<String> lgaCol = getTextColumn("LGA", TransactionReport.LGA);
        TextColumnBuilder<String> stateCol = getTextColumn("State", TransactionReport.STATE);
        TextColumnBuilder<String> statusCol = getTextColumn("Status", TransactionReport.STATUS);
        TextColumnBuilder<Date> dateCol = getDateColumn("Date Created", TransactionReport.TRANSACTION_DATE);

        TextColumnBuilder[] columns = {
                merchantCodeCol, merchantNameCol, emailCol, phoneCol,accountNoCol,acctNameCol,bankCol, lgaCol, stateCol, statusCol,dateCol};

        return generateReport(items, downloadType, MERCHANT_LIST,columns);
    }

    @Override
    protected ApplicationSettings getAppSettings() {
        return appSettings;
    }
}

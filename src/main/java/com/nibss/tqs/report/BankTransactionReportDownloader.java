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
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 9/9/2016.
 */
@Component
@Scope("prototype")
public class BankTransactionReportDownloader extends AbstractReportDownloader {

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
        Arrays.asList(
                "Transaction ID",
                "Biller Name",
                "Product",
                "Customer ID",
                "Transaction Date",
                "Date Approved",
                "Amount (N)",
                "Trxn Fee (N)",
                "Status",
                "");
         */
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> billNameCol = getTextColumn("Biller",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> prodCol = getTextColumn("Product",TransactionReport.PRODUCT);
        TextColumnBuilder<String> branchCodeCol = getTextColumn("Branch Code",TransactionReport.BRANCH_CODE);
        TextColumnBuilder<String> custIdCol = getTextColumn("Customer ID",TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<Date> txnDateCol = getDateColumn("Transaction Date",TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<Date> dtAppCol = getDateColumn("Date Approved",TransactionReport.DATE_APPROVED);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Fee (N)",TransactionReport.FEE);
        TextColumnBuilder<String> statCol = getTextColumn("Status",TransactionReport.STATUS);


        TextColumnBuilder[] columns = new TextColumnBuilder[] {
                sessIdCol,billNameCol,prodCol,branchCodeCol,custIdCol,txnDateCol,dtAppCol,amtCol,feeCol,statCol
        };

        return generateReport(items,downloadType,EBILLSPAY,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateCpayCardReport(List<AjaxCpayCardTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getCardTransactions(transactions);

        return null;
//        return generateReport(items,downloadType,CPAYCARD,columns);
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
                "PAYMENT TYPE",
                "PRODUCT",
                "AMOUNT (N)",
                "TRANSACTION DATE",
                "Status");
         */

        TextColumnBuilder<String> merCol = getTextColumn("Merchant", TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID", TransactionReport.SESSION_ID);
        TextColumnBuilder<String> cpayRefCol = getTextColumn("CPAY Ref.", TransactionReport.CPAY_REF);
        TextColumnBuilder<String> merchantRefCol = getTextColumn("Merchant Ref.", TransactionReport.MERCHANT_REF);
        TextColumnBuilder<String> payTypeCol = getTextColumn("Payment Type", TransactionReport.PAYMENT_TYPE);
        TextColumnBuilder<String> customerCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<String> prodCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<Date> txnDateCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder[] columns = new TextColumnBuilder[] {
                merCol,sessIdCol,cpayRefCol,merchantRefCol,payTypeCol,customerCol,prodCol,amtCol,txnDateCol,statCol
        };

        return generateReport(items,downloadType,CPAYACCOUNT,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateMerchantPayReport(List<AjaxMcashTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getMerchantPayTransactions(transactions);

        /*
        Arrays.asList("MERCHANT",
                "TELCO", "USSD AGGR.",
                 "SESSION ID", "PAYMENT REF.", "AMOUNT (N)",
                "TRANSACTION DATE", "STATUS");
         */
        TextColumnBuilder<String> merCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> merCodeCol = getTextColumn("Merchant Code",TransactionReport.MERCHANT_CODE);
        TextColumnBuilder<String> telCol = getTextColumn("Telco",TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggCol = getTextColumn("USSD Aggr.",TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> refCodeCol = getTextColumn("Reference Code", TransactionReport.REFERENCE_CODE);
        TextColumnBuilder<String> payRefCol = getTextColumn("Payment Ref.",TransactionReport.PAYMENT_REF);
        TextColumnBuilder<String> phoneNoCol = getTextColumn("Phone No.",TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)",TransactionReport.AMOUNT);
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Fee (N)",TransactionReport.FEE);
        TextColumnBuilder<Date> txnDateCol = getDateColumn("Transaction Date",TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statusCol = getTextColumn("Debit Status",TransactionReport.STATUS);
        TextColumnBuilder[] columns = new TextColumnBuilder[] {
                merCol,merCodeCol,telCol,ussdAggCol,sessIdCol,refCodeCol,payRefCol,phoneNoCol,amtCol,feeCol,txnDateCol,statusCol
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
                "AMOUNT (N)",
                "REQUEST TIME",
                "SOURCE STATUS",
                "DESTINATION STATUS"
        );
         */
        TextColumnBuilder<String> merCol = getTextColumn("Merchant", TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID", TransactionReport.SESSION_ID);
        TextColumnBuilder<String> trxnRefCol = getTextColumn("Transaction Ref", TransactionReport.TRANSACTION_REF);
        TextColumnBuilder<String> telCol = getTextColumn("Telco", TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggrCol = getTextColumn("USSD Aggr.", TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<Date> txnDateCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);

        TextColumnBuilder<String> srcStatCol = getTextColumn("Source Status", TransactionReport.SOURCE_RESPONSE_DESCRIPTION);
        TextColumnBuilder<String> srcDestCol = getTextColumn("Destination Status", TransactionReport.DESTINATION_RESPONSE_DESCRIPTION);

        TextColumnBuilder[] columns = new TextColumnBuilder[] {
                merCol,sessIdCol,trxnRefCol,telCol,ussdAggrCol,amtCol,txnDateCol,srcStatCol,srcDestCol
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
        TextColumnBuilder<String> phoneCol = getTextColumn("Phone No.", TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<String> acctNoCol = getTextColumn("Account Number", TransactionReport.ACCOUNT_NUMBER);
        TextColumnBuilder<String> acctNameCol = getTextColumn("Account Name", TransactionReport.ACCOUNT_NAME);
        TextColumnBuilder<String> lgaCol = getTextColumn("LGA", TransactionReport.LGA);
        TextColumnBuilder<String> stateCol = getTextColumn("State", TransactionReport.STATE);
        TextColumnBuilder<String> statusCol = getTextColumn("Status", TransactionReport.STATUS);
        TextColumnBuilder<Date> dateCol = getDateColumn("Date Created", TransactionReport.TRANSACTION_DATE);

        TextColumnBuilder[] columns = {
                merchantCodeCol, merchantNameCol, emailCol, phoneCol,acctNoCol,acctNameCol, lgaCol, stateCol, statusCol,dateCol};

        return generateReport(items, downloadType, MERCHANT_LIST,columns);
    }
}

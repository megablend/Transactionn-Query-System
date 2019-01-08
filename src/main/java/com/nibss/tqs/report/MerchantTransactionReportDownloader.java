package com.nibss.tqs.report;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.ajax.*;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.core.repositories.OrganizationSettingRepository;
import com.nibss.tqs.ebillspay.dto.UserParam;
import com.nibss.tqs.ebillspay.repositories.UserParamRepository;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.ListOfArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;

import static net.sf.dynamicreports.report.builder.DynamicReports.report;

/**
 * Created by eoriarewo on 9/9/2016.
 */
@Component
@Scope("prototype")
public class MerchantTransactionReportDownloader extends AbstractReportDownloader {

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private UserParamRepository userParamRepository;

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private OrganizationSettingRepository orgSetRepo;

    @Override
    protected ApplicationSettings getAppSettings() {
        return appSettings;
    }

    @Override
    public ByteArrayOutputStream generateEbillspayReport(List<AjaxEbillsPayTransaction> transactions, DownloadType downloadType) throws Exception {

        List<String> columnNames = new ArrayList<>();


        List<Object[]> data = new ArrayList<>();

        columnNames.add("Session ID");
        columnNames.add("Product");
        columnNames.add("Source Bank");
        columnNames.add("Branch Code");
        columnNames.add("Destination Bank");

        User loggedInUser = getUser();
        boolean showDateInitiated = false;
        if( null != loggedInUser) {
            if( orgSetRepo.findShowDateInitiatedByOrginzation(loggedInUser.getOrganizationInterface().getId())) {
                columnNames.add("Date Initiated");
                showDateInitiated = true;
            }

        }

        columnNames.add("Date Approved");
        columnNames.add("Amount");
        columnNames.add("TransactionFee");
        columnNames.add("Total");



        List<String> billerParams = null;
        if( loggedInUser != null) {
            Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(loggedInUser.getOrganizationInterface().getId());
            if( billerIds != null && !billerIds.isEmpty()) {
                billerParams = userParamRepository.getParamNamesForBiller(billerIds.iterator().next());
                if( null != billerParams && !billerParams.isEmpty())
                columnNames.addAll(billerParams);
            }
        }
        columnNames.add("Status");


        for(AjaxEbillsPayTransaction  t : transactions) {
            List<Object> temp = new ArrayList<>();
            temp.add( "'" + t.getSessionId());
            temp.add( t.getProductName());
            temp.add(t.getSourceBankName() == null ? "" : t.getSourceBankName());
            temp.add(t.getBranchCode());
            temp.add(t.getDestinationBankName()== null ? "" : t.getDestinationBankName());
            if(showDateInitiated)
                temp.add(t.getTransactionDate());
            temp.add(t.getDateApproved());
            BigDecimal tFee = t.getFee() == null ? BigDecimal.ZERO : t.getFee();

            temp.add(t.getAmount());
            temp.add(t.getFee());
            temp.add(t.getAmount().add(tFee));

            List<UserParam> lstParam = null;

            try {
                lstParam = userParamRepository.findBySessionId(t.getSessionId());
            } catch(Exception e) {}

            if( null == lstParam && ( null != billerParams && !billerParams.isEmpty())) {
                billerParams.forEach( x -> temp.add(""));
            } else {
                List<UserParam> params = lstParam;
                billerParams.forEach( s -> {
                    String value = getParamValueByName(params, s);
                    temp.add(value);
                });
            }

            temp.add(t.getResponseDescription());
            data.add( temp.toArray( new Object[0]));
        }


        TextColumnBuilder sessCol = getTextColumn("Transaction ID", "Session ID");
        TextColumnBuilder prodCol = getTextColumn("Product", "Product");
        TextColumnBuilder srcBankCol = getTextColumn("Source Bank", "Source Bank");
        TextColumnBuilder branchCodeCol = getTextColumn("Branch Code", "Branch Code");
        TextColumnBuilder destBankCol = getTextColumn("Destination Bank", "Destination Bank");

        TextColumnBuilder dtApprovedCol = getDateColumn("Date Approved", "Date Approved");

        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)", "Amount");
        TextColumnBuilder<BigDecimal> feeCol = getBigDecimalColumn("Transaction Fee (N))", "TransactionFee");
        TextColumnBuilder<BigDecimal> totCol = getBigDecimalColumn("Total (N)", "Total");
        TextColumnBuilder<String> statCol = getTextColumn("Status", "Status");


        List<TextColumnBuilder> builders = new ArrayList<>();
        builders.addAll(Arrays.asList(sessCol,prodCol,srcBankCol,branchCodeCol,destBankCol));
        if( showDateInitiated)
            builders.add(getDateColumn("Date Initiated", "Date Initiated"));
        builders.add(dtApprovedCol);
        builders.add(amtCol);
        builders.add(feeCol);
        builders.add(totCol);
        if( null != billerParams && !billerParams.isEmpty())
            billerParams.forEach( b -> builders.add( getTextColumn(b,b)));

        builders.add(statCol);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRDataSource source = new ListOfArrayDataSource(data,columnNames.toArray(new String[0]));
        TextColumnBuilder[] columns = builders.toArray(new TextColumnBuilder[0]);

        switch (downloadType) {
            default:
            case CSV:
                report().columns(columns).setDataSource(source).ignorePagination().toCsv(out);
                break;
            case EXCEL:
                report().columns(columns).setDataSource(source).ignorePagination().toXlsx(out);
                break;
            case PDF:
                PageType type = PageType.A4;
                PageOrientation orientation = PageOrientation.LANDSCAPE;
                out = generatePdfReport(source,EBILLSPAY,type,orientation,columns);
                break;
        }

        return out;
    }

    private String getParamValueByName(List<UserParam> params, String paramName) {
        if( null == params || params.isEmpty())
            return "";
        if( null == paramName || paramName.isEmpty())
            return "";
        UserParam param = params.stream().filter( p -> p.getName().equals(paramName)).findFirst().orElse(new UserParam());
        return param.getValue() == null ? "" : param.getValue();
    }


    private User getUser() {
        User user = null;
        SecurityContext holder = SecurityContextHolder.getContext();
        Authentication authentication = holder.getAuthentication();

        if( authentication != null) {
            user  = (User)authentication.getPrincipal();
        }
        return user;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateCpayCardReport(List<AjaxCpayCardTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getCardTransactions(transactions);

        /*
        Arrays.asList("GATEWAY",
                "CPAY REF",
                "MERCHANT REF",
                "PRODUCT",
                "AMOUNT",
                "TRANSACTION DATE",
                "STATUS");
         */
        TextColumnBuilder gatCol = getTextColumn("Gateway", TransactionReport.GATEWAY);
        TextColumnBuilder cpayRefCol = getTextColumn("CPAY Ref.", TransactionReport.CPAY_REF);
        TextColumnBuilder merRefCol = getTextColumn("Merchant Ref.", TransactionReport.MERCHANT_REF);
        TextColumnBuilder prodCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder custIdCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<Date> txnDtCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder[] columns = new TextColumnBuilder[] {
                gatCol,cpayRefCol,merRefCol,prodCol,custIdCol,amtCol,txnDtCol,statCol
        };


        return generateReport(items,downloadType,CPAYCARD,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateCpayAccountReport(List<AjaxCpayAccountTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getAccountTransactions(transactions);

        /*
         Arrays.asList(
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
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID", TransactionReport.SESSION_ID);
        TextColumnBuilder<String> cpayRefCol = getTextColumn("CPAY Ref.", TransactionReport.CPAY_REF);
        TextColumnBuilder<String> merRefCol = getTextColumn("Merchant Ref.", TransactionReport.MERCHANT_REF);
        TextColumnBuilder<String> srcBank = getTextColumn("Source Bank", TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> payTypeCol = getTextColumn("Payment Type", TransactionReport.PAYMENT_TYPE);
        TextColumnBuilder<String> customerCol = getTextColumn("Customer ID", TransactionReport.CUSTOMER_NUMBER);
        TextColumnBuilder<String> prodCol = getTextColumn("Product", TransactionReport.PRODUCT);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<Date> dtCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statCol = getTextColumn("Status", TransactionReport.STATUS);

        TextColumnBuilder[] columns = new TextColumnBuilder[] {
                sessIdCol,cpayRefCol,merRefCol,srcBank,payTypeCol,customerCol,prodCol,amtCol,dtCol,statCol
        };

        return generateReport(items,downloadType,CPAYACCOUNT,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateMerchantPayReport(List<AjaxMcashTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getMerchantPayTransactions(transactions);

        /*
        Arrays.asList(
        "MERCHANT", "MERCHANT CODE",
                "TELCO", "USSD AGGR.",
                "SOURCE BANK", "SESSION ID", "PAYMENT REF.", "AMOUNT (N)",
                "TRANSACTION DATE", "STATUS");
         */
        TextColumnBuilder<String> merCol = getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> merCodeCol = getTextColumn("Merchant Code",TransactionReport.MERCHANT_CODE);
        TextColumnBuilder<String> telCol = getTextColumn("Telco", TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggrcol = getTextColumn("USSD Aggr.", TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<String> srcBankCol = getTextColumn("Source Bank", TransactionReport.SOURCE_BANK);
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID", TransactionReport.SESSION_ID);
        TextColumnBuilder<String> refCodeCol = getTextColumn("Reference Code", TransactionReport.REFERENCE_CODE);
        TextColumnBuilder<String> payRefCol = getTextColumn("Payment Ref", TransactionReport.PAYMENT_REF);
        TextColumnBuilder<String> phoneNoCol = getTextColumn("Phone No.", TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<Date> dtCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> statCol = getTextColumn("Debit Status", TransactionReport.STATUS);
//        TextColumnBuilder<String> credStatCol = getTextColumn("Credit Status", TransactionReport.CREDIT_STATUS);

        TextColumnBuilder[] columns = new TextColumnBuilder[] {
              merCol,merCodeCol, telCol,ussdAggrcol,srcBankCol,sessIdCol,refCodeCol,payRefCol,phoneNoCol,amtCol,dtCol,statCol
                //,credStatCol
        };

        return generateReport(items,downloadType,USSD_MERCHANT_PAYMENT,columns);
    }

    @Override
    @Transactional
    public ByteArrayOutputStream generateUssdBillPaymentReport(List<AjaxUssdTransaction> transactions, DownloadType downloadType) throws Exception {
        List<TransactionReport> items = getUssdTransactions(transactions);

        /*
        Arrays.asList("SESSION ID",
                "TRANSACTION REFERENCE",
                "TELCO",
                "USSD AGGR.",
                "SOURCE BANK CODE",
                "AMOUNT (N)",
                "REQUEST TIME",
                "SOURCE STATUS",
                "DESTINATION STATUS");
         */
        TextColumnBuilder<String> sessIdCol = getTextColumn("Transaction ID", TransactionReport.SESSION_ID);
        TextColumnBuilder<String> trxnRefCol = getTextColumn("Transaction Ref.", TransactionReport.TRANSACTION_REF);
        TextColumnBuilder<String> phoneNoCol = getTextColumn("Phone Number", TransactionReport.PHONE_NUMBER);
        TextColumnBuilder<String> telCol = getTextColumn("Telco", TransactionReport.TELCO);
        TextColumnBuilder<String> ussdAggrCol = getTextColumn("USSD Aggr.", TransactionReport.USSD_AGGREGATOR);
        TextColumnBuilder<String> srcBankCode = getTextColumn("Source Bank Code", TransactionReport.SOURCE_BANK_CODE);
        TextColumnBuilder<BigDecimal> amtCol = getBigDecimalColumn("Amount (N)", TransactionReport.AMOUNT);
        TextColumnBuilder<Date> dtCol = getDateColumn("Transaction Date", TransactionReport.TRANSACTION_DATE);
        TextColumnBuilder<String> srcRespCol = getTextColumn("Source Status", TransactionReport.SOURCE_RESPONSE_DESCRIPTION);
        TextColumnBuilder<String> destRespCol = getTextColumn("Destination Status", TransactionReport.DESTINATION_RESPONSE_DESCRIPTION);

        TextColumnBuilder[] columns = new TextColumnBuilder[]{
                sessIdCol,trxnRefCol,phoneNoCol,telCol,ussdAggrCol,srcBankCode,amtCol,dtCol,srcRespCol,destRespCol
        };

        return generateReport(items,downloadType,USSD_BILL_PAYMENT,columns);
    }

    @Override
    public ByteArrayOutputStream generateMerchantList(List<Merchant> merchants, DownloadType downloadType) throws Exception {
        return null;
    }
}

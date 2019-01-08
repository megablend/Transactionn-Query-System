package com.nibss.tqs.report;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.util.BillingHelper;
import lombok.extern.slf4j.Slf4j;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalImageAlignment;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;


/**
 * Created by eoriarewo on 9/7/2016.
 */
@Component
@Scope("prototype")
@Slf4j
public class BillingReportGenerator {

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private BillingHelper billingHelper;

    public void generateReports(final Map<String,List<TransactionReport>> trxns, String productCode) throws Exception {

        billingHelper.createProductReportFolder(productCode);

        switch (productCode) {
            case Product.CENTRALPAY:
                for(String code : trxns.keySet())
                    createReportsForUssdBillingOrCpayAccountBilling(code,trxns.get(code),productCode, "Central Pay Account Billing");
                break;
            case Product.EBILLSPAY:
                for(String code : trxns.keySet())
                    createReportsForMpayOrEbillsPay(code, trxns.get(code),productCode,"e-BillsPay Billing Report");
                break;
            case Product.USSD_BILL_PAYMENT:
                for(String code : trxns.keySet())
                    createReportsForUssdBillingOrCpayAccountBilling(code,trxns.get(code),productCode, "USSD Billing Payment Billing");
                break;
            case Product.USSD_MERCHANT_PAYMENT:
                for(String code : trxns.keySet())
                    createReportsForMpayOrEbillsPay(code, trxns.get(code),productCode,"mCASH Billing Report");
                break;
            case Product.CLOUNGE_ANNUAL:
                for(String code : trxns.keySet())
                    createReportsForCLounge(code, trxns.get(code), productCode, "Corporate Lounge Annual Subscription Billing Report");
                break;
            case Product.CLOUNGE_PER_TRANSACTION:
                for(String code : trxns.keySet())
                    createReportsForCLounge(code, trxns.get(code), productCode, "Corporate Lounge Per-Transaction Billing Report");
                break;
        }

    }

    private void createReportsForCLounge(String code, List<TransactionReport> items, String productCode, String title) throws Exception {
        TextColumnBuilder<Integer> rowNumberCol = rowNumberColumn();
        TextColumnBuilder<String> acctNameCol = getTextColumn("Account Name",  "accountName");
        TextColumnBuilder<String> acctNoCol = getTextColumn("Account Number", "accountNumber");
        TextColumnBuilder<String> orgCol = getTextColumn("Organization Name", "organizationName");

        TextColumnBuilder<BigDecimal> feeCol = col.column("Fee (N)", "fee", type.bigDecimalType());
        TextColumnBuilder<BigDecimal> commissionCol = col.column("Commission", "calculatedAmount", type.bigDecimalType());

        TextColumnBuilder<String> sessionIdCol = getTextColumn("Session ID", "sessionId");
        TextColumnBuilder<Date> dateCreated = col.column("Request Date", "dateApproved", type.dateYearToMonthType());

        items.sort(Comparator.comparing(TransactionReport::getOrganizationName));

        ByteArrayOutputStream csvReport = null;

        if( productCode.equals(Product.CLOUNGE_PER_TRANSACTION))
            csvReport = generateCSVReport(items, rowNumberCol,sessionIdCol,orgCol,acctNameCol, acctNoCol, feeCol,commissionCol,dateCreated);
        else if( productCode.endsWith(Product.CLOUNGE_ANNUAL))
            csvReport = generateCSVReport(items,rowNumberCol,orgCol, acctNameCol, acctNoCol, feeCol, commissionCol);


        rowNumberCol.setFixedColumns(3).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        acctNameCol.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        acctNoCol.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        orgCol.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        commissionCol.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);
        sessionIdCol.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);


      /*  ByteArrayOutputStream pdfReport = null;

        if(productCode.equals(Product.CLOUNGE_PER_TRANSACTION))
            pdfReport = generatePdfReport(items,title,orgCol,commissionCol,rowNumberCol,acctNameCol,acctNoCol, orgCol,commissionCol);
        else
            pdfReport = generatePdfReport(items,title, orgCol, commissionCol,rowNumberCol,sessionIdCol,acctNameCol,acctNoCol,orgCol,commissionCol);
*/

        DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        String csvFileName = String.format("BillingReport_%s.csv", fmt.format(new Date()));
        String pdfFileName = String.format("BillingReport_%s.pdf", fmt.format(new Date()));

        billingHelper.createClientReportFolder(productCode,code);
        Path csvPath = Paths.get(appSettings.billingReportFolder(), productCode, code, csvFileName);
        Path pdfPath = Paths.get(appSettings.billingReportFolder(), productCode, code, pdfFileName);

        try(BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(csvPath, StandardOpenOption.CREATE))) {
            out.write(csvReport.toByteArray());
        }

      /*  try(BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pdfPath,StandardOpenOption.CREATE))) {
            out.write(pdfReport.toByteArray());
        }*/


    }


    private void  createReportsForUssdBillingOrCpayAccountBilling(String code, List<TransactionReport> items, String productCode, String title) throws Exception {


        TextColumnBuilder<Integer> rowNumberCol = rowNumberColumn();
        TextColumnBuilder<String> sessionIdCol = getTextColumn("Transaction ID", "sessionId");

        String colTitle = "USSD Transaction Ref.";
        if( productCode.equals(Product.CENTRALPAY))
            colTitle = "CPAY Transaction Ref";

        TextColumnBuilder<String> transactionRefCol = getTextColumn(colTitle,"transactionRef");
        TextColumnBuilder<String> merchantCol = getTextColumn("Merchant","merchantName");
        TextColumnBuilder<BigDecimal> commissionCol = getBigDecimalColumn("Commission (N)","calculatedAmount");

        items.sort(Comparator.comparing(TransactionReport::getMerchantName));

        //csv report
        ByteArrayOutputStream csvOutputStream = generateCSVReport(items, rowNumberCol,sessionIdCol,transactionRefCol,merchantCol,commissionCol);

        //pdf file
        rowNumberCol.setFixedColumns(3).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        merchantCol.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        commissionCol.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);
        sessionIdCol.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        transactionRefCol.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

//        ByteArrayOutputStream pdfOutputStream = generatePdfReport(items,title,merchantCol,commissionCol,rowNumberCol,sessionIdCol,transactionRefCol,merchantCol,commissionCol);

        DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        String csvFileName = String.format("BillingReport_%s.csv", fmt.format(new Date()));
        String pdfFileName = String.format("BillingReport_%s.pdf", fmt.format(new Date()));

        billingHelper.createClientReportFolder(productCode,code);
        Path csvPath = Paths.get(appSettings.billingReportFolder(), productCode, code, csvFileName);
        Path pdfPath = Paths.get(appSettings.billingReportFolder(), productCode, code, pdfFileName);

        try(BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(csvPath, StandardOpenOption.CREATE))) {
            out.write(csvOutputStream.toByteArray());
        }

      /*  try(BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pdfPath,StandardOpenOption.CREATE))) {
            out.write(pdfOutputStream.toByteArray());
        }*/

        log.trace("done generating reports");


    }

    private void createReportsForMpayOrEbillsPay(String code, List<TransactionReport> items, String productCode, String title) throws Exception{

        TextColumnBuilder<String> transactionIdColumn = getTextColumn("Transaction ID",TransactionReport.SESSION_ID);
        TextColumnBuilder<String> merchantColumn =  getTextColumn("Merchant",TransactionReport.MERCHANT_NAME);
        TextColumnBuilder<String> merchantCodeCol = getTextColumn("Merchant Code", TransactionReport.MERCHANT_CODE);
        TextColumnBuilder<BigDecimal> commissionColumn = getBigDecimalColumn("Commission (N)","calculatedAmount");

        TextColumnBuilder<String> branchCodeCol = getTextColumn("Branch Code", TransactionReport.BRANCH_CODE);

        TextColumnBuilder<Integer> rowNumberColumn = rowNumberColumn();


        items.sort(Comparator.comparing(TransactionReport::getMerchantName));
        //csv report: make sure to pass the texColumnBuilders in the order you want them displayed
        ByteArrayOutputStream csvOutputStream;
        if( productCode.equalsIgnoreCase(Product.USSD_MERCHANT_PAYMENT))
            csvOutputStream = generateCSVReport(items, rowNumberColumn,transactionIdColumn,merchantCodeCol,merchantColumn,commissionColumn);
        else //ebillspay
            csvOutputStream = generateCSVReport(items, rowNumberColumn,transactionIdColumn,branchCodeCol,merchantColumn,commissionColumn);

        //pdf file
        rowNumberColumn.setFixedColumns(3).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        merchantColumn.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        merchantCodeCol.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        commissionColumn.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);
        transactionIdColumn.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

      /*  ByteArrayOutputStream pdfOutputStream;

        if(productCode.equalsIgnoreCase(Product.USSD_MERCHANT_PAYMENT))
            pdfOutputStream = generatePdfReport(items, title, merchantCodeCol,commissionColumn, rowNumberColumn,transactionIdColumn,merchantCodeCol,merchantColumn,commissionColumn);
        else //ebillspay
            pdfOutputStream  = generatePdfReport(items, title, merchantColumn,commissionColumn, rowNumberColumn,transactionIdColumn,branchCodeCol,merchantColumn,commissionColumn);
*/

        DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        String csvFileName = String.format("BillingReport_%s.csv", fmt.format(new Date()));
        String pdfFileName = String.format("BillingReport_%s.pdf", fmt.format(new Date()));

        billingHelper.createClientReportFolder(productCode,code);
        Path csvPath = Paths.get(appSettings.billingReportFolder(), productCode, code, csvFileName);
        Path pdfPath = Paths.get(appSettings.billingReportFolder(), productCode, code, pdfFileName);

        try(BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(csvPath, StandardOpenOption.CREATE))) {
            out.write(csvOutputStream.toByteArray());
        }

       /* try(BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pdfPath,StandardOpenOption.CREATE))) {
            out.write(pdfOutputStream.toByteArray());
        }*/

        log.trace("done generating reports");
    }

    private ByteArrayOutputStream generateCSVReport( List<TransactionReport> items,TextColumnBuilder<? extends Serializable>... columns) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        report().columns(columns).setDataSource(new JRBeanCollectionDataSource(items)).ignorePagination().toCsv(outputStream);
        return outputStream;
    }

    private ByteArrayOutputStream generatePdfReport(List<TransactionReport> items, String rptTitle, TextColumnBuilder<? extends Serializable> groupByColumn,
                                                    TextColumnBuilder<? extends Number> subtotalColumn, TextColumnBuilder<? extends Serializable>... columns)
    throws  Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        StyleBuilder bodyStyle = stl.style().setFontSize(7);
        StyleBuilder boldStyle = stl.style().bold();
        StyleBuilder headerStyle = stl.style(boldStyle).setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT)
                .setVerticalImageAlignment(VerticalImageAlignment.MIDDLE).setFontSize(12).setForegroundColor(new Color(153,102,0));

        StyleBuilder columnTitleStyle = stl.style().bold().setBorder(stl.pen1Point()).setBackgroundColor(Color.LIGHT_GRAY).setFontSize(8)
                .setHorizontalImageAlignment(HorizontalImageAlignment.CENTER).setLeftPadding(5);

        StyleBuilder footerStyle = stl.style().setFontSize(5).setForegroundColor(new Color(210,210,210));

        JasperReportBuilder rptBuilder = report().columns(columns).setDataSource(new JRBeanCollectionDataSource(items)).title(
                cmp.horizontalList()
                        .add(cmp.image(ImageIO.read(appSettings.nibssLogo())).setFixedDimension(100, 40),
                                cmp.text(rptTitle).setStyle(headerStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                        ).newRow().add(cmp.filler().setStyle(stl.style()).setFixedHeight(10)))
                .pageFooter(cmp.horizontalList().add(
                        cmp.text(getFooterInfo()).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT).setStyle(footerStyle),
                        cmp.pageXofY().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(footerStyle)
                ))
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(bodyStyle)
                .highlightDetailEvenRows();


        if( null != groupByColumn)
            rptBuilder.groupBy(groupByColumn);
        if( null != subtotalColumn)
            rptBuilder.subtotalsAtSummary(sbt.sum(subtotalColumn));

        rptBuilder.toPdf(outputStream);

        return  outputStream;
    }
    private TextColumnBuilder<String> getTextColumn(String columnTitle, String fieldName) {
        return  col.column(columnTitle, fieldName, type.stringType());
    }

    private TextColumnBuilder<BigDecimal> getBigDecimalColumn(String columnTitle, String fieldName) {
        return  col.column(columnTitle,fieldName, type.bigDecimalType());
    }

    private TextColumnBuilder<Integer> rowNumberColumn() {
       
        return  col.reportRowNumberColumn("S/N");
    }

    public static String getFooterInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("\u00A9 ").append(LocalDate.now().getYear())
                .append(". Nigeria Inter-Bank Settlement System Plc")
                .append(". Date Generated: ")
                .append(new SimpleDateFormat("yyyy-MM-dd h:mm a").format(new Date()));

        return builder.toString();
    }
}

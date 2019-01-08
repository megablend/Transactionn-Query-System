package com.nibss.tqs.report;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.ajax.*;
import com.nibss.tqs.config.ApplicationSettings;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

/**
 * Created by eoriarewo on 9/8/2016.
 */
public  abstract class AbstractReportDownloader {

    protected static final String EBILLSPAY = "e-BillsPay Transactions Report";
    public static final String CPAYCARD = "CentralPay Card Transactions Report";
    public static final String CPAYACCOUNT = "CentralPay Account Transactions Report";
    public static final String USSD_BILL_PAYMENT = "USSD Bill Payment Transactions Report";
    public static final String USSD_MERCHANT_PAYMENT = "mCASH Transactions Report";

    public static final String MERCHANT_LIST = "mCash Merchants";

    protected ByteArrayOutputStream generateCsvReport(List<TransactionReport> items,TextColumnBuilder<? extends Object>... columns) throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        report().columns(columns).setDataSource(new JRBeanCollectionDataSource(items))
                .ignorePagination()
                .toCsv(outputStream);

        return outputStream;
    }

    protected ByteArrayOutputStream generateExcelReport(List<TransactionReport> items, TextColumnBuilder<? extends Object>... columns) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        report().columns(columns).setDataSource(new JRBeanCollectionDataSource(items))
                .ignorePagination()
                .toXlsx(outputStream);

        return outputStream;
    }

    protected ByteArrayOutputStream generatePdfReport(List<TransactionReport> items, String title, TextColumnBuilder<? extends Object>... columns) throws Exception {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        StyleBuilder bodyStyle = stl.style().setFontSize(7);
        StyleBuilder headerStyle = stl.style().bold().setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT)
                .setVerticalImageAlignment(VerticalImageAlignment.MIDDLE).setFontSize(12).setForegroundColor(new Color(153,102,0));

        StyleBuilder columnTitleStyle = stl.style().bold().setBorder(stl.pen1Point()).setBackgroundColor(Color.LIGHT_GRAY).setFontSize(8)
                .setHorizontalImageAlignment(HorizontalImageAlignment.CENTER).setLeftPadding(5);

        StyleBuilder footerStyle = stl.style().setFontSize(5).setForegroundColor(new Color(210,210,210));

        report().columns(columns).setDataSource(new JRBeanCollectionDataSource(items))
                .title(
                        cmp.horizontalList()
                                .add(cmp.image(ImageIO.read(getAppSettings().nibssLogo())).setFixedDimension(150, 40),
                                        cmp.text(title).setStyle(headerStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                                ).newRow().add(cmp.filler().setStyle(stl.style()).setFixedHeight(10)))
                .pageFooter(cmp.horizontalList().add(
                        cmp.text(BillingReportGenerator.getFooterInfo()).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT).setStyle(footerStyle),
                        cmp.pageXofY().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(footerStyle)
                ))
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(bodyStyle)
                .highlightDetailEvenRows()
                .toPdf(pdfStream);

        return pdfStream;
    }

    protected ByteArrayOutputStream generatePdfReport(JRDataSource dataSource, String title, PageType pageType,PageOrientation orientation, TextColumnBuilder<? extends Object>... columns) throws Exception {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        StyleBuilder bodyStyle = stl.style().setFontSize(7);
        StyleBuilder headerStyle = stl.style().bold().setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT)
                .setVerticalImageAlignment(VerticalImageAlignment.MIDDLE).setFontSize(12).setForegroundColor(new Color(153,102,0));

        StyleBuilder columnTitleStyle = stl.style().bold().setBorder(stl.pen1Point()).setBackgroundColor(Color.LIGHT_GRAY).setFontSize(8)
                .setHorizontalImageAlignment(HorizontalImageAlignment.CENTER).setLeftPadding(5);

        StyleBuilder footerStyle = stl.style().setFontSize(5).setForegroundColor(new Color(210,210,210));

        report().columns(columns).setDataSource(dataSource)
                .title(
                        cmp.horizontalList()
                                .add(cmp.image(ImageIO.read(getAppSettings().nibssLogo())).setFixedDimension(150, 40),
                                        cmp.text(title).setStyle(headerStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                                ).newRow().add(cmp.filler().setStyle(stl.style()).setFixedHeight(10)))
                .pageFooter(cmp.horizontalList().add(
                        cmp.text(BillingReportGenerator.getFooterInfo()).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT).setStyle(footerStyle),
                        cmp.pageXofY().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(footerStyle)
                ))
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(bodyStyle)
                .highlightDetailEvenRows()
                .setPageFormat(pageType, orientation)
                .toPdf(pdfStream);

        return pdfStream;
    }

    protected TextColumnBuilder<String> getTextColumn(String label, String fieldName) {
        return  col.column(label,fieldName,type.stringType());
    }

    protected TextColumnBuilder<BigDecimal> getBigDecimalColumn(String label,String fieldName) {
        return  col.column(label,fieldName,type.bigDecimalType());
    }

    protected TextColumnBuilder<Date> getDateColumn(String label, String fieldName) {
        return  col.column(label, fieldName, type.dateYearToSecondType());
    }

    protected ByteArrayOutputStream generateReport(List<TransactionReport> items, DownloadType downloadType,String title, TextColumnBuilder<? extends Serializable>[] columns) throws Exception {
        ByteArrayOutputStream out = null;
        switch (downloadType) {
            case CSV:
                out = generateCsvReport(items,columns);
                break;
            case EXCEL:
                out = generateExcelReport(items,columns);
                break;
            case PDF:
                out = generatePdfReport(items,title,columns);
                break;
        }

        return out;
    }

    protected List<TransactionReport> getEbillsTransactions(List<AjaxEbillsPayTransaction> transactions) {
        return transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());
    }
    protected  List<TransactionReport> getUssdTransactions(List<AjaxUssdTransaction> transactions) {
        return transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());
    }
    protected List<TransactionReport> getCardTransactions(List<AjaxCpayCardTransaction> transactions) {
        return  transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());
    }

    protected List<TransactionReport> getAccountTransactions(List<AjaxCpayAccountTransaction> transactions) {
        return  transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());
    }

    protected List<TransactionReport> getMerchantPayTransactions(List<AjaxMcashTransaction> transactions) {
        return  transactions.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());
    }

    protected  abstract  ApplicationSettings getAppSettings();

    public abstract ByteArrayOutputStream generateEbillspayReport(List<AjaxEbillsPayTransaction> transactions, DownloadType downloadType) throws Exception;

    public abstract ByteArrayOutputStream generateCpayCardReport(List<AjaxCpayCardTransaction> transactions, DownloadType downloadType) throws Exception;

    public abstract ByteArrayOutputStream generateCpayAccountReport(List<AjaxCpayAccountTransaction> transactions, DownloadType downloadType) throws Exception;

    public abstract ByteArrayOutputStream generateMerchantPayReport(List<AjaxMcashTransaction> transactions, DownloadType downloadType) throws Exception;

    public abstract  ByteArrayOutputStream generateUssdBillPaymentReport(List<AjaxUssdTransaction> transactions, DownloadType downloadType) throws Exception;

    public abstract ByteArrayOutputStream generateMerchantList(List<Merchant> merchants, DownloadType downloadType) throws Exception;
}

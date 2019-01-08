package com.nibss.tqs.corporatelounge.reports;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto;
import com.nibss.tqs.report.BillingReportGenerator;
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
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Date;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

/**
 * Created by eoriarewo on 10/5/2017.
 */

@Component
@Scope("prototype")
public class BalanceRequestReport {


    @Autowired
    private ApplicationSettings appSettings;

    //ByteArrayOutputStream pdfOutputStream = generatePdfReport(items, title, merchantColumn,commissionColumn,
    // rowNumberColumn,transactionIdColumn,merchantColumn,commissionColumn);

    public ByteArrayOutputStream generateReport(Collection<AccountBalanceDto> dtos) throws Exception {
        synchronized (new Object()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            TextColumnBuilder<String> acctNameCol = col.column("Account Name", AccountBalanceDto.ACCOUNT_NAME, type.stringType());
            TextColumnBuilder<String> acctNumberCol = col.column("Account Number", AccountBalanceDto.ACCOUNT_NUMBER, type.stringType());
            TextColumnBuilder<String> orgCol = col.column("Organization", AccountBalanceDto.ORGANIZATION, type.stringType());
            TextColumnBuilder<String> bankCol = col.column("Bank", AccountBalanceDto.BANK, type.stringType());
            TextColumnBuilder<Date> dateCol = col.column("Request Date", AccountBalanceDto.DATE, type.dateYearToMonthType());


            StyleBuilder bodyStyle = stl.style().setFontSize(7);
            StyleBuilder boldStyle = stl.style().bold();
            StyleBuilder headerStyle = stl.style(boldStyle).setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT)
                    .setVerticalImageAlignment(VerticalImageAlignment.MIDDLE).setFontSize(12).setForegroundColor(new Color(153,102,0));

            StyleBuilder columnTitleStyle = stl.style().bold().setBorder(stl.pen1Point()).setBackgroundColor(Color.LIGHT_GRAY).setFontSize(8)
                    .setHorizontalImageAlignment(HorizontalImageAlignment.CENTER).setLeftPadding(5);

            StyleBuilder footerStyle = stl.style().setFontSize(5).setForegroundColor(new Color(210,210,210));

            JasperReportBuilder rptBuilder = report().columns(
                    acctNameCol,acctNumberCol, orgCol,bankCol,dateCol
            ).setDataSource(new JRBeanCollectionDataSource(dtos)).title(
                    cmp.horizontalList()
                            .add(cmp.image(ImageIO.read(appSettings.nibssLogo())).setFixedDimension(100, 40),
                                    cmp.text("Account Balance Request Report").setStyle(headerStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
                            ).newRow().add(cmp.filler().setStyle(stl.style()).setFixedHeight(10)))
                    .pageFooter(cmp.horizontalList().add(
                            cmp.text(BillingReportGenerator.getFooterInfo()).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT).setStyle(footerStyle),
                            cmp.pageXofY().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(footerStyle)
                    ))
                    .setColumnTitleStyle(columnTitleStyle)
                    .setColumnStyle(bodyStyle)
                    .highlightDetailEvenRows();


                rptBuilder.groupBy(orgCol);

            rptBuilder.toPdf(outputStream);

            return  outputStream;
        }
    }
}

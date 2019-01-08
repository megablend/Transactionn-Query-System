package com.nibss.tqs.centralpay.renderer;

import com.nibss.tqs.ajax.AjaxCpayAccountTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.TransactionSummary;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.report.NibssTransactionReportDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Emor on 8/1/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NibssCpayAccountRenderer implements CpayAccountsRenderer {

    @Autowired
    private NibssTransactionReportDownloader transactionReportDownloader;

    @Override
    public List<String> getTableHeader() {
        return Arrays.asList(
                "MERCHANT",
                "SESSION ID",
                "CPAY REF",
                "MERCHANT_TXN_REF",
                "SOURCE BANK",
                "PAYMENT TYPE",
                "CUSTOMER ID",
                "PRODUCT",
                "NET AMOUNT (N)",
                "FEE (N)",
                "TRANSACTION DATE",
                "Status");
    }

    @Override
    @Transactional
    public JQueryDataTableResponse<Object> render(List<AjaxCpayAccountTransaction> transactions, JQueryDataTableRequest request, User user) {
        JQueryDataTableResponse<Object> response = new JQueryDataTableResponse<>();
        response.setDraw(request.getDraw());
        List<AjaxCpayAccountTransaction> limitedTrxn = getLimitedTransaction(transactions,request);
        response.setData( buildTransactionJson(limitedTrxn).toArray(new Object[0]));

        List<TransactionSummary> summaries  = getSummary(transactions,true);
        Map<String,Object> map = new HashMap<>();
        map.put("summary",summaries);
        map.put("showFee",true);
        response.setExtras(map);
        return response;
    }


    private List<Object[]> buildTransactionJson(final List<AjaxCpayAccountTransaction> transactions) {


        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
        NumberFormat numberFormat = new DecimalFormat(NUMBER_FORMAT_PATTERN);
        List<Object[]> values = new ArrayList<>();

        transactions.forEach( t -> {
            List<Object> temp = new ArrayList<>();
            temp.add(t.getMerchantName());
            temp.add(t.getSourceSessionId());
            temp.add(t.getCpayRef());
            temp.add(t.getMerchantRef());
            temp.add(t.getSourceBankName());
            temp.add(t.getPaymentType());
            temp.add( t.getCustomerId());
            temp.add(t.getProductName());
            temp.add( numberFormat.format(t.getAmount()));
            temp.add(numberFormat.format(t.getFee()));
            temp.add(fmt.format(t.getTransactionDate()));
            temp.add( t.getResponseDescription() == null ? "" : t.getResponseDescription());
            values.add(temp.toArray());
        });

        return values;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream download(DownloadType downloadType, List<AjaxCpayAccountTransaction> transactions) {
       try {
           return transactionReportDownloader.generateCpayAccountReport(transactions,downloadType);
       } catch(Exception e) {
           throw new RuntimeException(e);
       }
    }
}

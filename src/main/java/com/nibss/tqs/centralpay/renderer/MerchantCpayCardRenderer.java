package com.nibss.tqs.centralpay.renderer;

import com.nibss.tqs.ajax.AjaxCpayCardTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.TransactionSummary;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.report.MerchantTransactionReportDownloader;
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
public class MerchantCpayCardRenderer implements CpayCardRenderer {

    @Autowired
    private MerchantTransactionReportDownloader reportDownloader;

    @Override
    public List<String> getTableHeader() {
        return Arrays.asList("GATEWAY",
                "CPAY REF",
                "MERCHANT REF",
                "PRODUCT",
                "CUSTOMER ID",
                "AMOUNT",
                "TRANSACTION DATE",
                "STATUS");
    }

    @Override
    @Transactional
    public JQueryDataTableResponse<Object> render(List<AjaxCpayCardTransaction> transactions, JQueryDataTableRequest request, User user) {
        JQueryDataTableResponse<Object> response = new JQueryDataTableResponse<>();
        response.setDraw(request.getDraw());
        List<AjaxCpayCardTransaction> limitedTrxn = getLimitedTransaction(transactions,request);
        response.setData( buildTransactionJson(limitedTrxn).toArray(new Object[0]));

        List<TransactionSummary> summaries  = getSummary(transactions);
        Map<String,Object> map = new HashMap<>();
        map.put("summary",summaries);
        response.setExtras(map);
        return response;
    }


    private List<Object[]> buildTransactionJson(final List<AjaxCpayCardTransaction> transactions) {


        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
        NumberFormat numberFormat = new DecimalFormat(NUMBER_FORMAT_PATTERN);
        List<Object[]> values = new ArrayList<>();

        transactions.forEach( t -> {
            List<Object> temp = new ArrayList<>();
            temp.add(t.getPaymentGateway() == null ? "N/A" : t.getPaymentGateway());
            temp.add(t.getCpayRef());
            temp.add(t.getMerchantRef());
            temp.add(t.getProductName() == null ? "" : t.getProductName());
            temp.add(t.getCustomerId() == null ? "" : t.getCustomerId().replace("+", " "));
            temp.add( numberFormat.format(t.getAmount()));
            temp.add(fmt.format(t.getTransactionDate()));
            temp.add( t.getResponseDescription());
            values.add(temp.toArray());
        });

        return values;
    }
    @Override
    @Transactional
    public ByteArrayOutputStream download(DownloadType downloadType, List<AjaxCpayCardTransaction> transactions) {
        try {
            return reportDownloader.generateCpayCardReport(transactions,downloadType);
        } catch (Exception e) {
           throw new RuntimeException("cld nt gen card rpt",e);
        }
    }


}

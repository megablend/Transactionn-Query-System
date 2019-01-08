package com.nibss.tqs.ussd.renderer;

import com.nibss.tqs.ajax.AjaxUssdTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.TransactionSummary;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.BankTransactionReportDownloader;
import com.nibss.tqs.report.DownloadType;
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
 * Created by eoriarewo on 9/6/2016.
 */
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BankBillPaymentRenderer implements BillPaymentRenderer {


    @Autowired
    private BankTransactionReportDownloader reportDownloader;

    @Override
    public JQueryDataTableResponse<Object> render(List<AjaxUssdTransaction> transactions, JQueryDataTableRequest request, User user) {
        JQueryDataTableResponse<Object> response = new JQueryDataTableResponse<>();
        response.setDraw(request.getDraw());
        List<AjaxUssdTransaction> limitedTrxn = getLimitedTransaction(transactions,request);
        response.setData( buildTransactionJson(limitedTrxn).toArray(new Object[0]));

        List<TransactionSummary> summaries  = getSummary(transactions,true);
        Map<String,Object> map = new HashMap<>();
        map.put("summary",summaries);
        map.put("showFee",true);
        response.setExtras(map);
        return response;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream download(DownloadType downloadType, List<AjaxUssdTransaction> transactions) {
        try {
            return reportDownloader.generateUssdBillPaymentReport(transactions,downloadType);
        } catch (Exception e) {
            throw new RuntimeException("cld not gen ussd bill.  rpt",e);
        }
    }

    @Override
    public List<String> getTableHeader() {
        return Arrays.asList("MERCHANT",
                "SESSION ID",
                "TRANSACTION REFERENCE",
                "PHONE NUMBER",
                "TELCO",
                "USSD AGGR.",
                "AMOUNT (N)",
                "REQUEST TIME",
                "SOURCE STATUS",
                "DESTINATION STATUS"
        );
    }

    private List<Object[]> buildTransactionJson(final List<AjaxUssdTransaction> transactions) {

        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
        NumberFormat numberFormat = new DecimalFormat(NUMBER_FORMAT_PATTERN);
        List<Object[]> values = new ArrayList<>();

        transactions.forEach( t -> {
            List<Object> temp = new ArrayList<>();
            temp.add(t.getMerchantName());
            temp.add(t.getSessionId());
            temp.add(t.getId());
            temp.add( t.getPhoneNumber() == null ? "" : t.getPhoneNumber());
            temp.add(t.getTelcoName());
            temp.add(t.getUssdAggregator());
            temp.add( numberFormat.format(t.getAmount()));
            temp.add(fmt.format(t.getRequestTime()));
            temp.add( t.getDebitResponseDescription());
            temp.add( t.getCreditResponseDescription());
            values.add(temp.toArray());
        });

        return values;
    }
}

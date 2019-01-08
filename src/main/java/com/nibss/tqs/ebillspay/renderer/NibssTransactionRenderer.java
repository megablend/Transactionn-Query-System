package com.nibss.tqs.ebillspay.renderer;

import com.nibss.tqs.ajax.AjaxEbillsPayTransaction;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.renderer.EbillsTransactionRenderer;
import com.nibss.tqs.renderer.TransactionSummary;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.report.NibssTransactionReportDownloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by eoriarewo on 7/8/2016.
 */
@Component
@Slf4j
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NibssTransactionRenderer implements EbillsTransactionRenderer {

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private NibssTransactionReportDownloader reportDownloader;

    public NibssTransactionRenderer() {
    }

    @Override
    public JQueryDataTableResponse<Object> render(List<AjaxEbillsPayTransaction> transactions, JQueryDataTableRequest request, User user) {
        JQueryDataTableResponse<Object> response = new JQueryDataTableResponse<>();
        response.setDraw(request.getDraw());

        List<AjaxEbillsPayTransaction> limitTrxn = getLimitedTransactions(transactions,request);
        response.setData( buildTransactionJson(limitTrxn).toArray(new Object[0]));

        List<TransactionSummary> summaries = getSummary(transactions, true);
        Map<String,Object> map = new HashMap<>();
        map.put("showFee",true);
        map.put("summary",summaries);
        response.setExtras(map);
        return response;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream download(DownloadType downloadType, List<AjaxEbillsPayTransaction> transactions) throws IOException {
        try {
            return reportDownloader.generateEbillspayReport(transactions,downloadType);
        } catch (Exception e) {
           throw new IOException(e);
        }
    }

    @Override
    public List<String> getTableHeader() {
        return Arrays.asList("Transaction ID","Biller Name","Product","Source Bank","Branch Code","Destination Bank", "Customer ID",
                "Transaction Date","Date Approved","Amount (N)","Fee (N)","Status","");
    }


    private List<Object[]> buildTransactionJson(final List<AjaxEbillsPayTransaction> transactions) {


        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
        NumberFormat numberFormat = new DecimalFormat(NUMBER_FORMAT_PATTERN);
        List<Object[]> values = new ArrayList<>();

        transactions.forEach( t -> {

            List<Object> temp = new ArrayList<>();
            temp.add(appSettings.ebillspayUserParamsUrl().replace("sessionId",t.getSessionId()));
            temp.add(t.getBillerName());
            temp.add(t.getProductName());
            temp.add(t.getSourceBankName());
            temp.add(t.getBranchCode());
            temp.add(t.getDestinationBankName());
            temp.add(t.getCustomerNumber() == null ? "" : t.getCustomerNumber());
            temp.add(fmt.format(t.getTransactionDate()));
            temp.add( t.getDateApproved() == null ? "" : fmt.format(t.getDateApproved()));
            temp.add( numberFormat.format(t.getAmount()));
            temp.add( numberFormat.format(t.getFee()));
            temp.add( t.getResponseDescription());

            if( t.getResponseCode().equalsIgnoreCase(SUCCCESS_STATUS))
                temp.add( appSettings.ebillspayReceiptUrl().replace("sessionId",t.getSessionId()) );
            else
                temp.add("");

            values.add(temp.toArray());

        });

        return values;
    }
}

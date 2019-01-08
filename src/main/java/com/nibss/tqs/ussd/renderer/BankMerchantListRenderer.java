package com.nibss.tqs.ussd.renderer;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.merchantpay.entity.MerchantAccount;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.BankTransactionReportDownloader;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.report.NibssTransactionReportDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by eoriarewo on 3/28/2017.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BankMerchantListRenderer implements MerchantListRenderer {

    @Autowired
    private BankTransactionReportDownloader reportDownloader;

    @Override
    public JQueryDataTableResponse<Object> render(List<Merchant> transactions, JQueryDataTableRequest request, User user) {
        JQueryDataTableResponse<Object> response = new JQueryDataTableResponse<>();
        response.setDraw(request.getDraw());
        List<Merchant> limitedTrxn = getLimitedTransaction(transactions,request);
        response.setData( buildJson(limitedTrxn).toArray(new Object[0]));
        return response;
    }

    @Override
    public ByteArrayOutputStream download(DownloadType downloadType, List<Merchant> merchants) {
        try {
            return reportDownloader.generateMerchantList( merchants, downloadType);
        } catch (Exception e) {
           throw new RuntimeException("could not generate merchant list report",e);
        }
    }

    @Override
    public List<String> getTableHeader() {
        return Arrays.asList("Merchant Code",
                "Name",
        "Email", "Phone Number","Account Number", "Account Name", "LGA", "State", "Status", "Date Created");
    }


    private List<Object[]> buildJson(List<Merchant> merchants) {
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
        List<Object[]> values = new ArrayList<>();

        merchants.forEach( m -> {
            List<Object> temp = new ArrayList<>();
            temp.add(m.getMerchantCode());
            temp.add(m.getMerchantName());
            temp.add(m.getEmail());
            temp.add(m.getPhoneNumber());
            if(m.getMerchantAccountList() != null && !m.getMerchantAccountList().isEmpty()) {
                MerchantAccount merchantAccount = m.getMerchantAccountList().get(0);
                temp.add(merchantAccount.getAccountNumber());
                temp.add(merchantAccount.getAccountName());
            } else {
                temp.add("");
                temp.add("");
            }
            temp.add(m.getLga());
            temp.add(m.getState());
            temp.add(m.getFlag().getName());
            temp.add( fmt.format( m.getCreatedDate()));
            values.add( temp.toArray());
        });

        return values;
    }
}

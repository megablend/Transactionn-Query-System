package com.nibss.tqs.ussd.renderer;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.TransactionRenderer;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 3/28/2017.
 */

public interface MerchantListRenderer extends TransactionRenderer {

    JQueryDataTableResponse<Object> render(List<Merchant> transactions, JQueryDataTableRequest request, User user);

    ByteArrayOutputStream download(DownloadType downloadType, List<Merchant> transactions);

    default  List<Merchant> getLimitedTransaction(List<Merchant> merchants, JQueryDataTableRequest request) {
        int startIndex = (int)(request.getStart() / request.getLength());
        return merchants.stream().skip( startIndex * request.getLength()).limit(request.getLength()).collect(Collectors.toList());
    }
}

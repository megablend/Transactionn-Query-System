package com.nibss.tqs.ussd.renderer;

import com.nibss.tqs.ajax.AjaxMcashTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.TransactionRenderer;
import com.nibss.tqs.renderer.TransactionSummary;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 9/6/2016.
 */
public interface MerchantPaymentRenderer extends TransactionRenderer {

    String SUCCESS = "00";
    JQueryDataTableResponse<Object> render(List<AjaxMcashTransaction> transactions, JQueryDataTableRequest request, User user);

    ByteArrayOutputStream download(DownloadType downloadType, List<AjaxMcashTransaction> transactions);

    default  List<AjaxMcashTransaction> getLimitedTransaction(List<AjaxMcashTransaction> transactions, JQueryDataTableRequest request) {
        int startIndex = request.getStart() / request.getLength();
        return transactions.stream().skip( startIndex * request.getLength()).limit(request.getLength()).collect(Collectors.toList());
    }

    default  List<TransactionSummary> getSummary(List<AjaxMcashTransaction> transactions,boolean showFee) {
        List<TransactionSummary> summaries = new ArrayList<>();
        List<AjaxMcashTransaction> successful = transactions.stream().filter( t -> t.getDebitResponseCode().equals(SUCCESS))
                .collect(Collectors.toList());

        TransactionSummary successSummary = new TransactionSummary();
        successSummary.setLabel("Approved or Completed Successfully");
        successSummary.setVolume(successful.size());
        BigDecimal total = successful.stream().map(t ->  t.getAmount()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);
        successSummary.setTotalAmount( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(total));

        if(showFee) {
            BigDecimal totalFee = successful.stream().map(  t-> t.getFee()).reduce( (a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
            successSummary.setTransactionFee(new DecimalFormat(NUMBER_FORMAT_PATTERN).format(totalFee));
        }
        summaries.add(successSummary);

        TransactionSummary otherSummary = new TransactionSummary();
        otherSummary.setLabel("Others");
        otherSummary.setVolume( transactions.size() - successful.size());
        List<AjaxMcashTransaction> others = transactions.stream().filter( t -> !(t.getDebitResponseCode().equals(SUCCESS)) )
                .collect(Collectors.toList());

        BigDecimal otherTotal = others.stream().map( t -> t.getAmount()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
        otherSummary.setTotalAmount( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(otherTotal));

        if(showFee) {
            BigDecimal totalFee = others.stream().map( t -> t.getFee()).reduce( (a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
            otherSummary.setTransactionFee(new DecimalFormat(NUMBER_FORMAT_PATTERN).format(totalFee));
        }

        summaries.add(otherSummary);

        return summaries;
    }
}

package com.nibss.tqs.centralpay.renderer;

import com.nibss.tqs.ajax.AjaxCpayAccountTransaction;
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
 * Created by Emor on 8/1/2016.
 */
public interface CpayAccountsRenderer  extends TransactionRenderer{

    String SUCCESS = "success";
    JQueryDataTableResponse<Object> render(List<AjaxCpayAccountTransaction> transactions, JQueryDataTableRequest request, User user);

    ByteArrayOutputStream download(DownloadType downloadType, List<AjaxCpayAccountTransaction> transactions);

    default  List<AjaxCpayAccountTransaction> getLimitedTransaction(List<AjaxCpayAccountTransaction> transactions, JQueryDataTableRequest request) {
        int startIndex = (int)(request.getStart() / request.getLength());
        return transactions.stream().skip( startIndex * request.getLength()).limit(request.getLength()).collect(Collectors.toList());
    }

    default  List<TransactionSummary> getSummary(List<AjaxCpayAccountTransaction> transactions, boolean showFee) {
        List<TransactionSummary> summaries = new ArrayList<>();
        List<AjaxCpayAccountTransaction> successful = transactions.stream().filter( t -> t.getResponseDescription() != null  &&
                t.getResponseDescription().toLowerCase().contains(SUCCESS))
                .collect(Collectors.toList());

        TransactionSummary successSummary = new TransactionSummary();
        successSummary.setLabel("Approved or Completed Successfully");
        successSummary.setVolume(successful.size());
        BigDecimal total = successful.stream().map(t ->  t.getAmount()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);
        successSummary.setTotalAmount( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(total));

        if(showFee) {
            BigDecimal totalFee = successful.stream().map( t -> t.getFee()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
            successSummary.setTransactionFee( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(totalFee));
        }

        summaries.add(successSummary);

        TransactionSummary otherSummary = new TransactionSummary();
        otherSummary.setLabel("Others");
        otherSummary.setVolume( transactions.size() - successful.size());
        List<AjaxCpayAccountTransaction> others = transactions.stream().filter( t -> t.getResponseDescription() == null ||
                !t.getResponseDescription().toLowerCase().contains(SUCCESS))
                .collect(Collectors.toList());

        BigDecimal otherTotal = others.stream().map( t -> t.getAmount()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
        otherSummary.setTotalAmount( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(otherTotal));

        if(showFee) {
            BigDecimal totalFee = others.stream().map( t -> t.getFee()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
            otherSummary.setTransactionFee( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(totalFee));
        }

        summaries.add(otherSummary);

        return summaries;
    }
}

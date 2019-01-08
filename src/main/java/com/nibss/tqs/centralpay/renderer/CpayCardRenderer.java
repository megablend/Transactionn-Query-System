package com.nibss.tqs.centralpay.renderer;

import com.nibss.tqs.ajax.AjaxCpayCardTransaction;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Emor on 8/1/2016.
 */
public interface CpayCardRenderer  extends TransactionRenderer{

    List<String> SUCCESSFUL = Arrays.asList("00","000");

    JQueryDataTableResponse<Object> render(List<AjaxCpayCardTransaction> transactions, JQueryDataTableRequest request, User user);
    ByteArrayOutputStream download(DownloadType downloadType, List<AjaxCpayCardTransaction> transactions);

    default  List<AjaxCpayCardTransaction> getLimitedTransaction(List<AjaxCpayCardTransaction> transactions, JQueryDataTableRequest request) {
        int startIndex = (int)(request.getStart() / request.getLength());
        return transactions.stream().skip( startIndex * request.getLength()).limit(request.getLength()).collect(Collectors.toList());
    }

    default  List<TransactionSummary> getSummary(List<AjaxCpayCardTransaction> transactions) {
        List<TransactionSummary> summaries = new ArrayList<>();
        List<AjaxCpayCardTransaction> successful = transactions.stream().
                filter( t ->  t.getResponseCode() != null && SUCCESSFUL.stream().anyMatch( s -> s.equals(t.getResponseCode())))
                .collect(Collectors.toList());

        TransactionSummary successSummary = new TransactionSummary();
        successSummary.setLabel("Approved or Completed Successfully");
        successSummary.setVolume(successful.size());
        BigDecimal total = successful.stream().map( t ->  t.getAmount()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
        successSummary.setTotalAmount( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(total));

        summaries.add(successSummary);

        TransactionSummary otherSummary = new TransactionSummary();
        otherSummary.setLabel("Others");
        otherSummary.setVolume( transactions.size() - successful.size());
        List<AjaxCpayCardTransaction> others = transactions.stream().filter( t -> t.getResponseCode() == null ||
                SUCCESSFUL.stream().noneMatch( s -> s.equals(t.getResponseCode())))
                .collect(Collectors.toList());

        BigDecimal otherTotal = others.stream().map( t -> t.getAmount()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
        otherSummary.setTotalAmount( new DecimalFormat(NUMBER_FORMAT_PATTERN).format(otherTotal));

        summaries.add(otherSummary);

        return summaries;
    }
}

package com.nibss.tqs.renderer;

import com.nibss.tqs.ajax.AjaxEbillsPayTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public interface EbillsTransactionRenderer extends TransactionRenderer {


    List<String> VALID_RESPONSES = Arrays.asList("Unapproved", "Reversed", "Approved or Completed Successfully");


    JQueryDataTableResponse<Object> render(List<AjaxEbillsPayTransaction> transactions, JQueryDataTableRequest request, User user);

    ByteArrayOutputStream download(DownloadType downloadType, List<AjaxEbillsPayTransaction> transactions) throws IOException;

    //move to desired page
    default List<AjaxEbillsPayTransaction> getLimitedTransactions(List<AjaxEbillsPayTransaction> transactions, JQueryDataTableRequest request) {
        int startIndex = request.getStart() / request.getLength();
        return transactions.stream().skip(startIndex * request.getLength()).limit(request.getLength()).collect(Collectors.toList());
    }

    default List<TransactionSummary> getSummary(List<AjaxEbillsPayTransaction> transactions, boolean showTransactionFee) {

        DecimalFormat fmt = new DecimalFormat(NUMBER_FORMAT_PATTERN);
        List<TransactionSummary> summaries = new ArrayList<>();

        Predicate<AjaxEbillsPayTransaction> approvedPred = t -> t.getResponseCode().equals(SUCCCESS_STATUS);
        Predicate<AjaxEbillsPayTransaction> unapprovedPred = t -> t.getResponseCode().equals("-1");
        Predicate<AjaxEbillsPayTransaction> otherPred = t -> !t.getResponseCode().equals(SUCCCESS_STATUS) && !t.getResponseCode().equals("-1");

        TransactionSummary approved = new TransactionSummary();
        TransactionSummary unapproved = new TransactionSummary();
        TransactionSummary others = new TransactionSummary();

        approved.setLabel("Approved or Completed Successfully && Cleared Cheques");
        unapproved.setLabel("Unapproved");
        others.setLabel("Others");

        approved.setVolume(transactions.stream().filter(approvedPred).count());
        others.setVolume(transactions.stream().filter(otherPred).count());
        unapproved.setVolume(transactions.stream().filter(unapprovedPred).count());

        if (showTransactionFee) {
            BigDecimal susTot = transactions.stream().filter(approvedPred)
                    .map(a -> a.getAmount()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            BigDecimal susFee = transactions.stream().filter(approvedPred)
                    .map(a -> a.getFee()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            approved.setTotalAmount(fmt.format(susTot));
            approved.setTransactionFee(fmt.format(susFee));


            BigDecimal otherTot = transactions.stream().filter(otherPred)
                    .map(a -> a.getAmount()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            BigDecimal otherFee = transactions.stream().filter(otherPred)
                    .map(a -> a.getFee()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            others.setTotalAmount(fmt.format(otherTot));
            others.setTransactionFee(fmt.format(otherFee));


            BigDecimal unapprovedTot = transactions.stream().filter(unapprovedPred)
                    .map(a -> a.getAmount()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            BigDecimal unapprovedFee = transactions.stream().filter(unapprovedPred)
                    .map(a -> a.getFee()).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);


            unapproved.setTotalAmount(fmt.format(unapprovedTot));
            unapproved.setTransactionFee(fmt.format(unapprovedFee));


        } else {

            BigDecimal totalAmt = transactions.stream().filter(approvedPred)
                    .map(a -> a.getAmount().add(a.getFee())).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            approved.setTotalAmount(fmt.format(totalAmt));

            BigDecimal otherAmt = transactions.stream().filter(otherPred)
                    .map(a -> a.getAmount().add(a.getFee())).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            others.setTotalAmount(fmt.format(otherAmt));

            BigDecimal unapprovedAmt = transactions.stream().filter(unapprovedPred)
                    .map(a -> a.getAmount().add(a.getFee())).reduce((a, b) -> a.add(b)).orElse(BigDecimal.ZERO);

            unapproved.setTotalAmount(fmt.format(unapprovedAmt));
        }

        summaries.add(approved);
        summaries.add(unapproved);
        summaries.add(others);



       /* Map<String,List<AjaxEbillsPayTransaction>> trxnByResponses = transactions.stream().collect(Collectors.groupingBy(t -> t.getResponseDescription()));


        trxnByResponses.forEach( (k,v) -> {

            TransactionSummary summary = new TransactionSummary();
            summary.setVolume( v.size());

            summary.setLabel(k);
            if(showTransactionFee) {
                BigDecimal totalAmount = v.stream().map( t -> t.getAmount()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
                BigDecimal txnFee = v.stream().map(t -> t.getFee()).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);

                summary.setTotalAmount(fmt.format(totalAmount));
                summary.setTransactionFee(fmt.format(txnFee));
            } else {
                BigDecimal totalAmount = v.stream().map( t -> t.getAmount().add(t.getFee())).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
                summary.setTotalAmount( fmt.format(totalAmount));
            }

            summaries.add(summary);

        });


        List<TransactionSummary> otherSummaries = summaries.stream().filter( t -> VALID_RESPONSES.stream().noneMatch(x -> x.equalsIgnoreCase(t.getLabel())))
                .collect(Collectors.toList());
        if( !otherSummaries.isEmpty()) {
            summaries.removeAll(otherSummaries);
            TransactionSummary otherSummary = new TransactionSummary();
            otherSummary.setVolume( otherSummaries.stream().mapToLong( t -> t.getVolume()).sum());
            otherSummary.setLabel("Other");
            BigDecimal totalAmount = otherSummaries.stream().map( t -> new BigDecimal(t.getTotalAmount().replace(",",""))).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
            otherSummary.setTotalAmount(fmt.format(totalAmount) );
            if(showTransactionFee) {
                BigDecimal txnFee = otherSummaries.stream().map( t -> new BigDecimal(t.getTransactionFee().replace(",",""))).reduce((a,b) -> a.add(b)).orElse(BigDecimal.ZERO);
                otherSummary.setTransactionFee(fmt.format(txnFee));
            }

            summaries.add(otherSummary);
        }*/

        return summaries;
    }
}

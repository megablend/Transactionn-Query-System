package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ajax.AjaxEbillsPayTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.BillingCycle;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.queries.QueryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public interface EbillsTransactionCustomRepo {

    long totalCount(User user);

    long filteredCount(QueryDTO queryDTO);

    List<AjaxEbillsPayTransaction> findTransactions(QueryDTO queryDTO, int start, int itemCount);

    List<EbillspayTransaction> getWeeklyTransactionTimeTransactions();

    List<EbillspayTransaction> getTransactionTimeTransactions(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<EbillspayTransaction> getCustomBillerTransactions(LocalDateTime startDateTime, LocalDateTime endDateTime, BillingCycle billingCycle);
    
    List<EbillspayTransaction> getBacklogsForTransactionTimeTaken();
    
    List<EbillspayTransaction> getBacklogsForNonTransactionTimeTaken();
}

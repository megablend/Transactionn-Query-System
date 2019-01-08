package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.ajax.AjaxMcashTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.queries.QueryDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by eoriarewo on 8/18/2016.
 */
public interface MerchantPaymentTransactionRepo {

    List<DebitTransaction> getTransactionsForWeeklyBilling();

    List<DebitTransaction> getTransactionsForBillingPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime);

    long totalCount(User user);

    long filteredCount(QueryDTO queryDTO);

    List<AjaxMcashTransaction> findTransactions(QueryDTO queryDTO, int start, int itemCount);
    
    List<DebitTransaction> getBacklogTransactions();
}

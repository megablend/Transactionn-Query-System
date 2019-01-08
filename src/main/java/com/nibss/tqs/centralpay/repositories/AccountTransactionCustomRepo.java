package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.ajax.AjaxCpayAccountTransaction;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.queries.QueryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public interface AccountTransactionCustomRepo {

    long filteredCount(QueryDTO queryDTO);

    long totalCount(final User user);

    List<AjaxCpayAccountTransaction> findTransactions(QueryDTO queryDTO, int start, int end);

    List<AccountTransaction> getTransactionsWeeklyBilling();

    List<AccountTransaction> getTransactionsForBacklogs();
    
    List<AccountTransaction> getTransactionsForBillingPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime);
}

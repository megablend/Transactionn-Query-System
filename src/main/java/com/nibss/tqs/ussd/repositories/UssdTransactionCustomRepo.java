package com.nibss.tqs.ussd.repositories;

import com.nibss.tqs.ajax.AjaxUssdTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.queries.QueryDTO;
import com.nibss.tqs.ussd.dto.UssdTransaction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by eoriarewo on 8/30/2016.
 */
public interface UssdTransactionCustomRepo {
    List<UssdTransaction> getTransactionsForWeeklyBilling();

    List<UssdTransaction> getTransactionsForPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime);

    long filteredCount(QueryDTO queryDTO);

    long totalCount(final User user);

    List<AjaxUssdTransaction> findTransactions(QueryDTO queryDTO, int start, int end);
    
    List<UssdTransaction> getBacklogTransactions();
}

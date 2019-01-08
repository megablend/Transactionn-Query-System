package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.ajax.AjaxCpayCardTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.queries.QueryDTO;

import java.util.List;

/**
 * Created by eoriarewo on 7/11/2016.
 */
public interface CardTransactionCustomRepo {

    long filteredCount(QueryDTO queryDTO);

    long totalCount(final User user);

    List<AjaxCpayCardTransaction> findTransactions(QueryDTO queryDTO, int start, int end);


}

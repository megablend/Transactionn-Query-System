package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.queries.QueryDTO;

import java.util.List;

/**
 * Created by eoriarewo on 4/6/2017.
 */
public interface MPMerchantRepositoryRepo {

    long totalCount(User user);

    long filteredCount(QueryDTO queryDTO);

    List<Merchant> findMerchants(QueryDTO queryDTO, int start, int itemCount);
}

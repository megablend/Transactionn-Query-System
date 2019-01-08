package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.AccountDetail;
import com.nibss.merchantpay.entity.DebitTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by eoriarewo on 8/16/2016.
 */
@Transactional
public interface MerchantPaymentTransactionRepository extends JpaRepository<DebitTransaction,Long>, MerchantPaymentTransactionRepo {

    @Query("SELECT t.accountDetail FROM Aggregator t WHERE t.aggregatorId = ?1 ")
    AccountDetail findForAggregatorId(long aggregatorId);

    @Query("SELECT t.accountDetail FROM Telco t WHERE t.telcoId = ?1 ")
    AccountDetail findForTelcoId(long telcoId);
}

package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.BillingCycle;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Transactional
public interface EbillsTransactionRepository extends JpaRepository<EbillspayTransaction,Integer>,EbillsTransactionCustomRepo {

    List<EbillspayTransaction> findUnsharedTransactions();

    @Query("SELECT t FROM BaseTransaction t WHERE t.sessionId= ?1 ")
    List<BaseTransaction> findBySessionId(String sessionId);
}

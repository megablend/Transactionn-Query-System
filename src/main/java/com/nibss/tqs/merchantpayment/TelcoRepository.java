package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.Telco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by eoriarewo on 8/18/2016.
 */
//@Transactional
public interface TelcoRepository extends JpaRepository<Telco, Long> {

    @Query("SELECT t FROM Telco t JOIN FETCH t.accountDetail WHERE t.telcoCode=?1")
    Telco findByTelcoCode(String telcoCode);
}

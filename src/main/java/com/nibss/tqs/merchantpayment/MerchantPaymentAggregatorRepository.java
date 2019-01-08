package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.Aggregator;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by eoriarewo on 8/16/2016.
 */
public interface MerchantPaymentAggregatorRepository extends JpaRepository<Aggregator,Long> {
}

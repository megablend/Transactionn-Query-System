package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.centralpay.dto.CpayAccountSharingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by eoriarewo on 9/14/2016.
 */
public interface CpaySharingConfigRepository extends JpaRepository<CpayAccountSharingConfig, Integer> {
}

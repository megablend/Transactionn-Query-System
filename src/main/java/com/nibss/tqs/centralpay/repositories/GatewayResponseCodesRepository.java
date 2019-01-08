package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.centralpay.dto.GatewayResponseCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by eoriarewo on 7/19/2017.
 */
public interface GatewayResponseCodesRepository extends JpaRepository<GatewayResponseCodes,Integer> {

    @Query("SELECT t.responseDescription FROM GatewayResponseCodes t WHERE t.responseCode= ?1 AND t.gatewayId = ?2")
    String findDescriptionByResponseCodeAndProcessor(String responseCode, String processorId);
}

package com.nibss.tqs.centralpay.service;

import com.nibss.tqs.centralpay.repositories.GatewayResponseCodesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by eoriarewo on 7/19/2017.
 */
@Service
public class GatewayResponseService {


    @Autowired
    private GatewayResponseCodesRepository gatewayRepo;

    @Cacheable(cacheNames = "cpayResponses")
    public String getDescriptionByResponseCodeAndProcessor(String responseCode, String processorId) {
        synchronized (new Object()) {
            return  gatewayRepo.findDescriptionByResponseCodeAndProcessor(responseCode, processorId);
        }
    }
}

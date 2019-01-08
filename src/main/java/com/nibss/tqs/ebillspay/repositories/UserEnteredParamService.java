package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ebillspay.dto.UserParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by eoriarewo on 6/14/2017.
 */
@Service
public class UserEnteredParamService {

    @Autowired
    private UserParamRepository userParamRepository;

    @Cacheable(cacheNames = "userEnteredParams", unless = "#result == null || #result.empty")
    public List<String> getParamNamesForBiller(int billerId) {
        return  userParamRepository.getParamNamesForBiller(billerId);
    }

    @Cacheable(cacheNames = "userEnteredParams", unless = "#result == null || #result.empty")
    public List<UserParam> findBySessionId(String sessionId) {
        return userParamRepository.findBySessionId(sessionId);
    }
}

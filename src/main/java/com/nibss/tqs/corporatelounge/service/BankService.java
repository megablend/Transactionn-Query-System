package com.nibss.tqs.corporatelounge.service;

import com.nibss.corporatelounge.dto.Bank;
import com.nibss.tqs.corporatelounge.repositories.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by eoriarewo on 5/3/2017.
 */
@Service("clBankService")
public class BankService {

    @Autowired
    @Qualifier("clBankRepo")
    private BankRepository bankRepository;


    @Cacheable(cacheNames ="clBanks", unless = "#result == null || #result.empty")
    public synchronized List<Bank> findAllStrippedDown() {
        return bankRepository.findAllStrippedDown();
    }

    @Cacheable(cacheNames = "clBanks", unless = "#result == null")
    public Bank findByCode(String accountBankCode) {
        return bankRepository.findByCode(accountBankCode);
    }

    @Cacheable(cacheNames = "clBanks", unless = "#result == null")
    public synchronized  String findNipCodeFromCbnCode(String cbnCode) {
        return bankRepository.findNipCodeFromCbnCode(cbnCode);
    }
}

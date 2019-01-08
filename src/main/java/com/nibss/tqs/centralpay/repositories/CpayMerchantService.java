package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.centralpay.dto.CpayMerchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by eoriarewo on 6/5/2017.
 */
@Service
public class CpayMerchantService {

    @Autowired
    private CpayMerchantRepository merchantRepository;


    @Cacheable(value = "cpayMerchants", unless = "#result == null")
    public CpayMerchant findByName(String name) {
        return merchantRepository.findByName(name);
    }

    @Cacheable(value = "cpayMerchants", unless = "#result == null")
    public  CpayMerchant findByMerchantCode(String merchantCode) {
        return merchantRepository.findByMerchantCode(merchantCode);
    }

    @Cacheable(cacheNames = "cpayMerchants", unless = "#result == null || #result.empty")
    public List<CpayMerchant> findAll() {
        return merchantRepository.findAll();
    }

    @Cacheable(cacheNames = "cpayMerchants", unless = "#result == null || #result.empty")
    public List<CpayMerchant> findByNameLike(String name) {
        return merchantRepository.findByNameLike(name);
    }

    @Cacheable(cacheNames = "cpayMerchants", unless = "#result == null || #result.empty")
    public List<CpayMerchant> findByMerchantCodes(Iterable<String> centralPayMerchantCodes) {
        return merchantRepository.findByMerchantCodes(centralPayMerchantCodes);
    }

    @Cacheable(cacheNames = "cpayMerchants", unless = "#result == null || #result.empty")
    public List<SearchDTO> searchAll() {
        return merchantRepository.searchAll();
    }

    public Page<SearchDTO> searchAll(Pageable pageable) {
        synchronized (new Object()) {
            return merchantRepository.searchAll(pageable);
        }
    }

    public Page<SearchDTO> searchAll(String text, Pageable pageable) {
        synchronized (new Object()) {
            return merchantRepository.searchAll(text,pageable);
        }
    }
}

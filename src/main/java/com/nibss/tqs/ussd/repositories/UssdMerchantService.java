package com.nibss.tqs.ussd.repositories;

import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.ussd.dto.UssdBiller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by eoriarewo on 6/5/2017.
 */
@Service
public class UssdMerchantService {

    @Autowired
    private UssdBillerRepository ussdRepo;

    @Cacheable(value = "ussdBillers", unless = "#result == null || #result.empty")
    public List<UssdBiller> findByNameLike(String name) {
        return  ussdRepo.findByNameLike(name);
    }

    @Cacheable(value = "ussdBillers", unless = "#result == null || #result.empty")
    public List<UssdBiller> findAll() {
        return ussdRepo.findAll();
    }

    @Cacheable(value = "ussdBillers", unless = "#result == null || #result.empty")
    public List<UssdBiller> findByCodes(Iterable<String> codes) {
        return ussdRepo.findByCodes(codes);
    }

    @Cacheable(value = "ussdBillers", unless = "#result == null || #result.empty")
    public List<SearchDTO> searchAll() {
        return  ussdRepo.searchAll();
    }

    public Page<SearchDTO> searchall(Pageable pageable) {
        synchronized (new Object()) {
            return  ussdRepo.searchAll(pageable);
        }
    }

    public Page<SearchDTO> searchAll(String text, Pageable pageable) {
        synchronized (new Object()) {
            return ussdRepo.searchAll(text,pageable);
        }
    }
}

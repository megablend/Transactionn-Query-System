package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.ebillspay.dto.Biller;
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
public class EbillsBillerService {

    @Autowired
    private BillerRepository billerRepository;


    @Cacheable(value = "ebillspayBillers", unless = "#result == null || #result.empty")
    public List<Biller> findAll() {
        return billerRepository.findAll();
    }

    @Cacheable(value = "ebillspayBillers", unless = "#result == null || #result.empty")
    public List<Biller> findAll(Iterable<Integer> billerIds) {
        return  billerRepository.findAll(billerIds);
    }


    @Cacheable(value = "ebillspayBillers", unless = "#result == null || #result.empty")
    public List<SearchDTO> searchAll() {
        return billerRepository.searchAll();
    }

    public Page<SearchDTO> searchAll(Pageable pageable) {
        synchronized (new Object()) {
            return billerRepository.searchAll(pageable);
        }
    }

    public Page<SearchDTO> searchAll(String text, Pageable pageable) {
        synchronized (new Object()) {
            return billerRepository.searchAll(text,pageable);
        }
    }
}

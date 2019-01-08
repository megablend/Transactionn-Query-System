package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.ajax.SearchDTO;
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
public class McashService {

    @Autowired
    private MPMerchantRepository merchantRepository;

    @Cacheable(value = "merchantPay", unless = "#result == null || #result.empty")
    public List<Merchant> findAll() {
        return  merchantRepository.findAll();
    }

    @Cacheable(value = "merchantPay", unless = "#result == null || #result.empty")
    public List<Long> findIdsByMerchantCode(List<String> merchantCodes) {
        return merchantRepository.findIdsByMerchantCode(merchantCodes);
    }

    @Cacheable(value = "merchantPay", unless = "#result == null || #result.empty")
    public List<SearchDTO> searchAll() {
        return  merchantRepository.searchAll();
    }

//    @Cacheable(value = "merchantPay", unless = "#result == null || #result.content.empty")
    public Page<SearchDTO> searchAll(Pageable pageable) {
        synchronized (new Object()) {
            return merchantRepository.searchAll(pageable);
        }
    }

//    @Cacheable(value = "merchantPay", unless = "#result == null || #result.content.empty")
    public Page<SearchDTO> searchAll(String text, Pageable pageable) {
        synchronized (new Object()) {
            return merchantRepository.searchAll(text,pageable);
        }
    }
}

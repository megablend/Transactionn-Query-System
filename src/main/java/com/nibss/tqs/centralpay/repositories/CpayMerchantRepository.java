package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.centralpay.dto.CpayMerchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by eoriarewo on 7/20/2016.
 */
public interface CpayMerchantRepository extends JpaRepository<CpayMerchant,Integer> {

    @Cacheable("cpayMerchants")
    CpayMerchant findByName(String name);

    @Cacheable("cpayMerchants")
    CpayMerchant findByMerchantCode(String merchantCode);

    @Cacheable(cacheNames = "cpayMerchants")
    List<CpayMerchant> findAll();

    @Cacheable(cacheNames = "cpayMerchants")
    List<CpayMerchant> findByNameLike(String name);

    List<CpayMerchant> findByMerchantCodes(Iterable<String> centralPayMerchantCodes);

    @Query("SELECT new com.nibss.tqs.ajax.SearchDTO( b.merchantCode,b.name) FROM CpayMerchant b")
    List<SearchDTO> searchAll();

    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(m.merchantCode,m.name) from CpayMerchant m order by m.name",
    countQuery = "select count(m) from CpayMerchant m")
    Page<SearchDTO> searchAll(Pageable pageable);

    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(m.merchantCode,m.name) from CpayMerchant m where m.merchantCode like ?1 or m.name like ?1 " +
            "order by m.name",
    countQuery = "select count(m) from CpayMerchant m where m.name like ?1 or m.merchantCode like ?1")
    Page<SearchDTO> searchAll(String text, Pageable pageable);

    @Query(value = "SELECT new com.nibss.tqs.ajax.SearchDTO( b.merchantCode,b.name) FROM CpayMerchant b WHERE b.merchantCode in :codes",
    countQuery = "SELECT COUNT( b) FROM CpayMerchant b WHERE b.merchantCode in :codes")
    Page<SearchDTO> findBasicByMerchantCodes(@Param("codes") Collection<String> codes, Pageable pageable);
}

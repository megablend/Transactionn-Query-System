package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.ajax.SearchDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Created by eoriarewo on 8/24/2016.
 */
public interface MPMerchantRepository extends JpaRepository<Merchant,Long>, MPMerchantRepositoryRepo {

    @Cacheable("merchantPay")
    List<Merchant> findByMerchantNameLike( String name);

    @Cacheable("merchantPay")
    List<Merchant> findAll();

    @Query("SELECT m.merchantId FROM Merchant m WHERE m.merchantCode IN ?1")
    List<Long> findIdsByMerchantCode(Collection<String> merchantCodes);

    @Query("SELECT new com.nibss.tqs.ajax.SearchDTO(m.merchantId,m.merchantName) FROM Merchant m")
    List<SearchDTO> searchAll();

    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(m.merchantId,m.merchantName) from Merchant m order by m.merchantName",
    countQuery = "select count(m) from Merchant m")
    Page<SearchDTO> searchAll(Pageable pageable);

    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(m.merchantId,m.merchantName) from Merchant m " +
            " where m.merchantName like ?1 or m.merchantCode like ?1 order by m.merchantName",
    countQuery = "select count(m) from Merchant m where m.merchantCode like ?1 or m.merchantName like ?1")
    Page<SearchDTO> searchAll(String text, Pageable pageable);

    @Query(value = "SELECT new com.nibss.tqs.ajax.SearchDTO(m.merchantCode,m.merchantName) FROM Merchant m  WHERE m.merchantId IN :ids",
    countQuery = "SELECT COUNT(m) FROM Merchant m  WHERE m.merchantId IN :ids")
    Page<SearchDTO> findAllByIds(@Param("ids") Collection<Long> ids, Pageable pageable);
}

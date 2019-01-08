package com.nibss.tqs.ussd.repositories;

import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.ussd.dto.UssdBiller;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Created by eoriarewo on 8/22/2016.
 */
public interface UssdBillerRepository extends JpaRepository<UssdBiller, Integer> {

    @Cacheable("ussdBillers")
    List<UssdBiller> findByNameLike(String name);

    @Cacheable("ussdBillers")
    List<UssdBiller> findAll();

    List<UssdBiller> findByCodes(Iterable<String> codes);

    @Query("SELECT new com.nibss.tqs.ajax.SearchDTO(b.merchantCode,b.name) FROM UssdBiller b")
    List<SearchDTO> searchAll();


    @Query(value = "SELECT new com.nibss.tqs.ajax.SearchDTO(b.merchantCode,b.name) FROM UssdBiller b WHERE b.merchantCode in :codes",
    countQuery = "SELECT COUNT(b) FROM UssdBiller b WHERE b.merchantCode in :codes")
    Page<SearchDTO> findBasicByCodes(@Param("codes") Collection<String> codes, Pageable pageable);


    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(b.merchantCode,b.name) from UssdBiller b ",
            countQuery = "select count(b) from UssdBiller b")
    Page<SearchDTO> searchAll(Pageable pageable);


    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(b.merchantCode,b.name) from UssdBiller b " +
            " where b.merchantCode like ?1 or b.name like ?1 order by b.name",
    countQuery = "select count(b) from UssdBiller b where b.merchantCode like ?1 or b.name like ?1")
    Page<SearchDTO> searchAll(String text, Pageable pageable);
}

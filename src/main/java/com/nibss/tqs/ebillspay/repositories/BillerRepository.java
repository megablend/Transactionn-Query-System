package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.ebillspay.dto.Biller;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Emor on 7/2/16.
 */
@Transactional
public interface BillerRepository extends JpaRepository<Biller, Integer>, BillerCustomRepo {

    @Query("SELECT b FROM Biller b WHERE b.approved = true")
    List<Biller> findAll();

    List<Biller> findAll(Iterable<Integer> billerIds);


    @Query("SELECT new com.nibss.tqs.ajax.SearchDTO(b.id,b.name) FROM Biller b WHERE b.approved = true")
    List<SearchDTO> searchAll();

    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(b.id,b.name) from Biller b where b.approved = true order by b.name",
    countQuery = "select count(b) from Biller b where b.approved = true")
    Page<SearchDTO> searchAll(Pageable pageable);

    @Query(value = "select new com.nibss.tqs.ajax.SearchDTO(b.id,b.name) from Biller b where b.approved = true and b.name like ?1 order by b.name",
    countQuery = "select count(b) from Biller b where b.approved = true and b.name like ?1")
    Page<SearchDTO> searchAll(String text, Pageable pageable);

    @Query(value = "SELECT b.name FROM Biller b  WHERE b.id IN :ids", countQuery = "SELECT COUNT(b) FROM Biller b WHERE b.id IN :ids")
    Page<String> findAllByIds(@Param("ids") Collection<Integer> ebillspayBillerIds, Pageable pageable);
}

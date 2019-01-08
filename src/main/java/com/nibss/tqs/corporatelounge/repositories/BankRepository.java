package com.nibss.tqs.corporatelounge.repositories;

import com.nibss.corporatelounge.dto.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Repository("clBankRepo")
public interface BankRepository extends JpaRepository<Bank, Long> {

    // TODO: 12/9/16  add some caching here
    @Query("SELECT b FROM Bank b WHERE b.cbnCode = ?1 OR b.nipCode = ?1")
    Bank findByCode(String code);

    @Query("SELECT new com.nibss.corporatelounge.dto.Bank(b.name, b.cbnCode) FROM Bank b")
    List<Bank> findAllStrippedDown();

    @Query("SELECT b.nipCode FROM Bank b WHERE b.cbnCode = ?1")
    String findNipCodeFromCbnCode(String cbnCode);
}

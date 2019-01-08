package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ebillspay.dto.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Created by eoriarewo on 7/28/2016.
 */
@Component("ebillspayBankRepo")
public interface BankRepository extends JpaRepository<Bank,Integer> {

    Bank findByCode(String cbnCode);
}

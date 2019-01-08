package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Bank;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@Primary
public interface BankRepository extends JpaRepository<Bank, Integer>{
	Bank findByCbnBankCode(String bankCode);
	int countByCbnBankCode(String bankCode);

	Bank findByNipCode(String nipCode);

	Bank findByCode(String code);

	int countByNipCode(String nipCode);

	@Query("SELECT b.cbnBankCode FROM Bank b WHERE b.id = ?1")
	String findCbnCodeByBank(int id);

	@Query("SELECT b.nipCode FROM Bank b WHERE b.id = ?1")
	String findNipCodeByBank(int id);
}

package com.nibss.tqs.ussd.repositories;

import com.nibss.tqs.ussd.dto.UssdTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by Emor on 8/18/16.
 */
public interface UssdTransactionRepository extends JpaRepository<UssdTransaction,String>,UssdTransactionCustomRepo {

}

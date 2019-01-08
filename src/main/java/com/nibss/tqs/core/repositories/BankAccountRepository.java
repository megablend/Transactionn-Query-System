package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.BankAccount;
import com.nibss.tqs.core.entities.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by eoriarewo on 8/18/2016.
 */
public interface BankAccountRepository extends JpaRepository<BankAccount,Integer> {

    BankAccount findByOrganizationAndProductCode(int orgId, String productCode);

    List<BankAccount> findByOrganization(int orgId);
}

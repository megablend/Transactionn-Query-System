package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ebillspay.dto.EbillsBillingConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by eoriarewo on 8/9/2016.
 */
@Transactional
public interface EbillsBillingConfigurationRepository extends JpaRepository<EbillsBillingConfiguration,Integer> {
    List<EbillsBillingConfiguration> findAll();

    @Query("SELECT   t FROM EbillsBillingConfiguration t WHERE t.biller.id = ?1")
    EbillsBillingConfiguration findForBiller(int billerId);


    @Query("SELECT new com.nibss.tqs.ebillspay.dto.EbillsBillingConfiguration(e.biller.name,e) FROM EbillsBillingConfiguration e")
    List<EbillsBillingConfiguration> findAllForRendering();
}

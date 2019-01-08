package com.nibss.tqs.corporatelounge.repositories;

import com.nibss.corporatelounge.dto.BillingSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by eoriarewo on 10/12/2017.
 */
public interface CorporateLoungeBillingSettingRepository extends JpaRepository<BillingSetting, Integer> {

    BillingSetting findFirstByOrderById();
}

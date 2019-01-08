package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.OrganizationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by Emor on 8/1/2016.
 */
public interface OrganizationSettingRepository extends JpaRepository<OrganizationSetting,Integer> {

    int countById(int id);

    @Query("SELECT o.ebillspayTransactionDateAllowed FROM OrganizationSetting o WHERE o.organization.id = ?1")
    Boolean findShowDateInitiatedByOrginzation(int orgId);


    @Query("SELECT o FROM OrganizationSetting o WHERE o.organization.id = ?1")
    OrganizationSetting findByOrganization(int orgId);

}

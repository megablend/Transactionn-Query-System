package com.nibss.tqs.corporatelounge.repositories;

import com.nibss.corporatelounge.dto.Organization;
import com.nibss.cryptography.IVKeyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Repository("clClientRepo")
public interface ClientRepository extends JpaRepository<Organization,Long> {

    List<Organization> findAll();

    @Query("SELECT new com.nibss.corporatelounge.dto.Organization(o.id,o.secretKey,o.ivKey,o.maxRequestSize) FROM Organization o WHERE o.institutionCode = ?1")
    Organization findByInstitutionCode(String institutionCode);

    long countByInstitutionCode(String institutionCode);

    @Transactional
    @Modifying
    @Query("UPDATE Organization o SET o.secretKey = ?1, o.ivKey = ?2 WHERE o.id = ?3")
    int updateOrganizationKeys(String secretKey, String ivKey, long orgId);
}

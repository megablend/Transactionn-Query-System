package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Emor on 7/9/16.
 */
@Transactional
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    List<Organization> findByOrganizationType(int organizationType);
    Organization findByName(String name);

    int countByName(String name);

    //all aggregators
    List<Organization> findAllEbillsPayAggregators();

    List<Organization> findAllMerchantPaymentAggregators();

    List<Organization> findAllCentralPayAggregators();

    List<Organization> findAllUssdAggregators();


    //aggregator for product
    List<Organization> findAggregatorForEbillsPayBiller(int billerId);

    List<Organization> findAggregatorForMerchantPaymentMerchant(long merchantpayId);

    List<Organization> findAggregatorForUssdBiller(String billerCode);

    List<Organization> findAggregatorForCentralPayMerchant(String merchantCode);


    //merchant per product
    List<Organization> findEbillspayMerchant(int billerId);

    List<Organization> findCentralPayMerchant(String merchantCode);

    List<Organization> findMerchantPayMerchant(long merchantId);

    List<Organization> findUssdMerchant(String merchantCode);

    @Query(value = "SELECT new com.nibss.tqs.ajax.AjaxOrganization(o.id, o.name, o.organizationType) FROM Organization o WHERE o.name LIKE ?1",
            countQuery = "SELECT COUNT(o) FROM Organization o WHERE o.name LIKE ?1")
    Page<IOrganization> findByNameContaining(String name, Pageable pageable);

    @Query(value = "SELECT new com.nibss.tqs.ajax.AjaxOrganization(o.id, o.name, o.organizationType) FROM Organization o WHERE o.organizationType = ?1",
    countQuery = "SELECT COUNT(o) FROM Organization o WHERE o.organizationType = ?1")
    Page<IOrganization> findByOrganizationType(int orgType, Pageable pageable);


    @Query("SELECT new com.nibss.tqs.ajax.AjaxOrganization(o.id, o.name, o.organizationType) FROM Organization o")
    Page<IOrganization> findAllByProjection(Pageable pageable);


    @Query("SELECT new com.nibss.tqs.ajax.AjaxOrganization(o.id, o.name, o.organizationType) FROM Organization o, IN(o.users) u WHERE u.id = ?1")
    IOrganization findByUser(int userId);


    @Query(value = "SELECT merchant_id FROM organization_merhantpay_merchants WHERE organization_id = ?1", nativeQuery = true)
    Collection<Number> findMerchantIdsByOrganization(int orgId);


    @Query(value = "SELECT biller_id FROM organization_ebills_billers WHERE organization_id = ?1", nativeQuery = true)
    Collection<Integer> findEbillsPayBillersForOrganization(int orgId);

    @Query(value = "SELECT merchant_id FROM organization_cpay_merchants WHERE organization_id = ?1", nativeQuery = true)
    Collection<String> findCentralPayMerchantCodesForOrganization(int orgId);


    @Query("select case when count(b) > 0 then true else false end from Organization o join o.ebillspayBillerIds b where b = ?1 and o.organizationType = ?2")
    boolean eBillsBillerInOrganizationType(int billerId, int organizationType);

    @Query("select case when count(m) > 0 then true else false end from Organization o join o.centralPayMerchantCodes m where m = ?1 and o.organizationType = ?2")
    boolean cpayMerchantInOrganizationType(String merchantCode, int organizationType);

    @Query("select case when count(m) > 0 then true else false end from Organization o join o.merchantPaymentIds m where m = ?1 and o.organizationType = ?2")
    boolean mcashMerchantInOrganizationType(long merchantId, int organizationType);

    @Query("select case when count(u) > 0 then true else false end from Organization o join o.ussdBillerCodes u where u = ?1 and o.organizationType = ?2")
    boolean ussdBillerInOrganiaztionType(String ussdBillerCode, int organizationType);



    @Modifying
    @Transactional
    @Query(value = "insert into organization_ebills_billers(biller_id, organization_id) values (?1,?2)", nativeQuery = true)
    int saveEbillsBiller(int billerId, int orgId);


    @Modifying
    @Transactional
    @Query(value = "insert into organization_merhantpay_merchants(merchant_id,organization_id) values (?1,?2)", nativeQuery = true)
    int saveMcashMerchant(long merchantId, int orgId);



    @Modifying
    @Transactional
    @Query(value = "insert into organization_cpay_merchants(merchant_id,organization_id) values (?1,?2)", nativeQuery = true)
    int saveCpayMerchant(String merchantCode, int orgId);


    @Modifying
    @Transactional
    @Query(value = "insert into organization_ussd_billers(ussd_merchant_code,organization_id) values (?1,?2)", nativeQuery = true)
    int saveUssdMerchant(String merchantCode, int orgId);


    @Modifying
    @Transactional
    @Query(value = "delete from organization_ussd_billers where  organization_id = ?1", nativeQuery = true)
    int deleteUssdMerchant(int orgId);


    @Modifying
    @Transactional
    @Query(value = "delete from organization_ebills_billers where  organization_id = ?1", nativeQuery = true)
    int deleteEBillsBiller(int orgId);


    @Modifying
    @Transactional
    @Query(value = "delete from organization_merhantpay_merchants where  organization_id = ?1", nativeQuery = true)
    int deleteMcashMerchant(int orgId);

    @Modifying
    @Transactional
    @Query(value = "delete from organization_cpay_merchants where  organization_id = ?1", nativeQuery = true)
    int deleteCpayMerchant(int orgId);


    @Modifying
    @Transactional
    @Query(value = "delete from organization_ebills_billers where biller_id = ?1 and organization_id in (select id from organizations where organization_type = ?2)", nativeQuery = true)
    int deleteEbillsBillerByIdAndOrganizationType(int billerId, int organizationType);

    @Modifying
    @Transactional
    @Query(value = "delete from organization_merhantpay_merchants where merchant_id = ?1 and organization_id in (select id from organizations where organization_type = ?2)", nativeQuery = true)
    int deleteMcashMerchantByIdAndOrganizationType(long merchantId, int organizationType);


    @Modifying
    @Transactional
    @Query(value = "delete from organization_cpay_merchants where merchant_id = ?1 and organization_id in (select id from organizations where organization_type = ?2)", nativeQuery = true)
    int deleteCpayMerchantByIdAndOrganizationType(String merchantId, int organizationType);

    @Modifying
    @Transactional
    @Query(value = "delete from organization_ussd_billers where ussd_merchant_code = ?1 and organization_id in (select id from organizations where organization_type = ?2)", nativeQuery = true)
    int deleteUssdMerchantByIdAndOrganizationType(String merchantId, int organizationType);




    @Query(value = "SELECT ussd_merchant_code FROM organization_ussd_billers WHERE organization_id = ?1", nativeQuery = true)
    Collection<String> findUssdBillerCodesForOrganization(int orgId);

    @Query("SELECT new com.nibss.tqs.ajax.AjaxOrganization(o.id, o.name, o.organizationType) FROM Organization o WHERE o.id = ?1")
    IOrganization findOneForDisplay(int orgId);
}

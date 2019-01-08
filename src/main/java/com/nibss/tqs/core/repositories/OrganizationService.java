package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by eoriarewo on 6/5/2017.
 */
@Service
@Transactional
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    public List<Organization> findByOrganizationType(int organizationType) {
        return organizationRepository.findByOrganizationType(organizationType);
    }


    public Organization findByName(String name) {
        return organizationRepository.findByName(name);
    }


    public int countByName(String name) {
        return organizationRepository.countByName(name);
    }


    public List<Organization> findAllEbillsPayAggregators() {
        return organizationRepository.findAllEbillsPayAggregators();
    }


    public List<Organization> findAllMerchantPaymentAggregators() {
        return organizationRepository.findAllMerchantPaymentAggregators();
    }


    public List<Organization> findAllCentralPayAggregators() {
        return organizationRepository.findAllCentralPayAggregators();
    }


    public List<Organization> findAllUssdAggregators() {
        return organizationRepository.findAllUssdAggregators();
    }


    public List<Organization> findAggregatorForEbillsPayBiller(int billerId) {
        return organizationRepository.findAggregatorForEbillsPayBiller(billerId);
    }


    public List<Organization> findAggregatorForMerchantPaymentMerchant(long merchantpayId) {
        return organizationRepository.findAggregatorForMerchantPaymentMerchant(merchantpayId);
    }


    public List<Organization> findAggregatorForUssdBiller(String billerCode) {
        return organizationRepository.findAggregatorForUssdBiller(billerCode);
    }


    public List<Organization> findAggregatorForCentralPayMerchant(String merchantCode) {
        return organizationRepository.findAggregatorForCentralPayMerchant(merchantCode);
    }


    public List<Organization> findEbillspayMerchant(int billerId) {
        return organizationRepository.findEbillspayMerchant(billerId);
    }


    public List<Organization> findCentralPayMerchant(String merchantCode) {
        return organizationRepository.findCentralPayMerchant(merchantCode);
    }


    public List<Organization> findMerchantPayMerchant(long merchantId) {
        return organizationRepository.findMerchantPayMerchant(merchantId);
    }


    public List<Organization> findUssdMerchant(String merchantCode) {
        return organizationRepository.findUssdMerchant(merchantCode);
    }


    @CacheEvict(cacheNames = "organizations")
    public Organization save(Organization s) {
        return organizationRepository.save(s);
    }


    @Cacheable(cacheNames = "organizations", unless = "#result == null")
    public Organization findOne(Integer integer) {
        return organizationRepository.findOne(integer);
    }


    public boolean exists(Integer integer) {
        return organizationRepository.exists(integer);
    }


    @Cacheable(cacheNames = "organizations", unless = "#result == null || #result.empty")
    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }


    public List<Organization> findAll(Sort sort) {
        return organizationRepository.findAll(sort);
    }


    public Page<Organization> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable);
    }


    public List<Organization> findAll(Iterable<Integer> integers) {
        return organizationRepository.findAll(integers);
    }


    public long count() {
        return organizationRepository.count();
    }


    @CacheEvict(cacheNames = "organizations")
    public void delete(Integer integer) {

        organizationRepository.delete(integer);
    }


    @CacheEvict(cacheNames = "organizations")
    public void delete(Organization organization) {

        organizationRepository.delete(organization);
    }


    @CacheEvict(cacheNames = "organizations")
    public void delete(Iterable<? extends Organization> iterable) {
        organizationRepository.delete(iterable);
    }


    @CacheEvict(cacheNames = "organizations")
    public void deleteAll() {
        organizationRepository.deleteAll();
    }


    public void flush() {

        organizationRepository.flush();
    }


    @CacheEvict(cacheNames = "organizations")
    public void deleteInBatch(Iterable<Organization> entities) {

        organizationRepository.deleteInBatch(entities);
    }


    @CacheEvict(cacheNames = "organizations")
    public void deleteAllInBatch() {
        organizationRepository.deleteAllInBatch();
    }


    @Cacheable(cacheNames = "organizations", unless = "#result == null")
    public Organization getOne(Integer integer) {
        return organizationRepository.getOne(integer);
    }


    @CacheEvict(cacheNames = "organizations")
    public Organization saveAndFlush(Organization organization) {
        return organizationRepository.saveAndFlush(organization);
    }


    @CacheEvict(cacheNames = "organizations")
    public List<Organization> save(Iterable<Organization> entities) {
        return organizationRepository.save(entities);
    }
}

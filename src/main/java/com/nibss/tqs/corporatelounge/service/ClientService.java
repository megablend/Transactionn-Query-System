package com.nibss.tqs.corporatelounge.service;

import com.nibss.corporatelounge.dto.Organization;
import com.nibss.cryptography.IVKeyPair;
import com.nibss.tqs.corporatelounge.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Service("clClientService")
public class ClientService {

    @Autowired
    @Qualifier("clClientRepo")
    private ClientRepository clientRepository;

    @Cacheable(cacheNames = {"clClients"}, unless = "#result == null")
    public synchronized Organization findByInstitutionCode(String institutionCode) {
        return  clientRepository.findByInstitutionCode(institutionCode);
    }

    @Cacheable(cacheNames = {"clClients"}, unless = "#result == null || #result.empty")
    public synchronized List<Organization> findAll() {
        return clientRepository.findAll();
    }

    @CacheEvict(cacheNames = "clClients")
    public synchronized Organization save(final Organization organization) {
        return clientRepository.save(organization);
    }

    @CacheEvict(cacheNames = "clClients")
    public synchronized Organization saveAndFlush(final Organization organization) {
        return clientRepository.saveAndFlush(organization);
    }

    @Cacheable(cacheNames = {"clClients"}, unless = "#result == null")
    public synchronized  Organization findById(long id) {
        return clientRepository.findOne(id);
    }


    public synchronized long countByInstitutionCode(String institutionCode) {
        return clientRepository.countByInstitutionCode(institutionCode);
    }

    public synchronized long count() {
        return clientRepository.count();
    }

    @CacheEvict(cacheNames = "clClients")
    public synchronized int updateOrganizationKeys(String secretKey, String ivKey, long orgId) {
        return clientRepository.updateOrganizationKeys(secretKey, ivKey, orgId);
    }
}

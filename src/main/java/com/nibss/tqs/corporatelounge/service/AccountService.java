package com.nibss.tqs.corporatelounge.service;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountStatus;
import com.nibss.corporatelounge.dto.Organization;
import com.nibss.corporatelounge.dto.PaymentMode;
import com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto;
import com.nibss.tqs.corporatelounge.ajax.AccountDto;
import com.nibss.tqs.corporatelounge.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Service
public class AccountService {

    @Autowired
    @Qualifier("clAcctRepo")
    private AccountRepository accountRepository;


    public  Set<Account> findAllByOrganization(long orgId) {
        return accountRepository.findAllByOrganization(orgId);
    }

    public  Set<AccountDto> findAllByOrganizationForView(long orgId) {
        return accountRepository.findAllByOrganizationForView(orgId);
    }

    //@Cacheable(cacheNames = "clOrgAccts", unless = "#result == null || #result.empty")
    public  Set<Account> findByStatusAndOrganization(AccountStatus accountStatus, long orgId) {
        return accountRepository.findByStatusAndOrganization(accountStatus, orgId);
    }

   // @Cacheable(cacheNames = "clOrgAccts", unless = "#result == null || #result.empty")
    public  Set<Account> findByStatusAndOrganization(AccountStatus status, long orgId, List<String> accountNos) {
        return accountRepository.findByStatusAndOrganization(status,orgId,accountNos);
    }

   // @Cacheable(cacheNames = "clOrgAccts", unless = "#result.content.empty")
    public   Page<Account> findAllByOrganization(long orgId, int startPage, int size) {
        Pageable pageable = new PageRequest(startPage,size);
        return  accountRepository.findAllByOrganization(orgId, pageable);
    }

   // @Cacheable(cacheNames = "clOrgAccts", unless = "#result.content.empty")
    public   Page<Account> findStrippedByOrganization(long orgId, int startPage, int size) {
        Pageable pageable = new PageRequest(startPage,size);
        return  accountRepository.findStrippedByOrganization(orgId, pageable);
    }

    public  long countAccountByOrganization(long orgId, String accountNumber, String cbnBankCode) {
        return accountRepository.countAccountByOrganization(orgId,accountNumber, cbnBankCode);
    }

    @CacheEvict(cacheNames = "clOrgAccts")
    public  Account save(final Account account) {
        return  accountRepository.save(account);
    }

    @CacheEvict(cacheNames = "clOrgAccts")
    public  Account saveAndFlush(final Account account) {
        return accountRepository.saveAndFlush(account);
    }

    @CacheEvict(cacheNames = "clOrgAccts")
    public  List<Account> save(Iterable<Account> accounts) {
        return accountRepository.save(accounts);
    }

//    @Cacheable(cacheNames = "clOrgAccts")
    public  List<Account> findAll(List<Long> ids) {
        synchronized (new Object()) {
            return accountRepository.findAll(ids);
        }
    }

    public  List<Account> findByAccountNumberAndOrganization(String accountNumber, Organization organization) {
        return accountRepository.findByAccountNumberAndOrganization(accountNumber,organization.getId());
    }

    public Account findOrganizationAccount(String acctNumber, String cbnBankCode, long orgId) {
        return accountRepository.findOrganizationAccount(acctNumber,cbnBankCode,orgId);
    }
    public  List<Account> findStrippedByAccountNumberAndOrganization(String accountNumber, Organization organization) {
        return accountRepository.findStrippedByAccountNumberAndOrganization(accountNumber,organization.getId());
    }

    public   int updateAccountEmail(long acctId, String email) {
        assert null != email && !email.trim().isEmpty() : "email is null or empty";
        return accountRepository.updateAccountEmail(acctId, email);
    }




   public int updateAccountStatusAndDates(long acctId, AccountStatus accountStatus, Date dateActive, Date expiryDate) {
        synchronized (new Object()) {
            if(accountStatus == null)
                throw new NullPointerException("accountStatus cannot be null");

            if( accountStatus == AccountStatus.APPROVED) {
                if( null == dateActive || null == expiryDate)
                    throw new IllegalStateException("dateActive and expiryDate cannot be null for APPROVED status");
            }

            return accountRepository.updateAccountStatusAndDates(acctId,accountStatus,dateActive,expiryDate);
        }
   }

    public List<Account> findForBilling(Date startDate, Date endDate, PaymentMode annual) {
        return accountRepository.findForBilling(startDate,endDate, annual);
    }
}

package com.nibss.tqs.corporatelounge.repositories;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountStatus;
import com.nibss.corporatelounge.dto.Organization;
import com.nibss.corporatelounge.dto.PaymentMode;
import com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto;
import com.nibss.tqs.corporatelounge.ajax.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Repository("clAcctRepo")
public interface AccountRepository extends JpaRepository<Account,Long> {

    @Query("SELECT a FROM Account a WHERE a.organization.id = ?1")
    Set<Account> findAllByOrganization(long orgId);

    @Query("SELECT a FROM Account a WHERE a.accountStatus = ?1 AND a.organization.id = ?2 ")
    Set<Account> findByStatusAndOrganization(AccountStatus status, long orgId);

    @Query("SELECT new com.nibss.corporatelounge.dto.Account(a.accountNumber,a.accountName,a.bank.cbnCode,a.accountStatus,a.mandateReference,a.paymentMode) FROM Account a WHERE a.accountStatus = ?1 AND a.organization.id = ?2 AND a.accountNumber IN ?3 ")
    Set<Account> findByStatusAndOrganization(AccountStatus status, long orgId, List<String> accountNos);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.organization.id = ?1 AND a.accountNumber = ?2 AND a.bank.cbnCode = ?3 ")
    long countAccountByOrganization(long orgId, String accountNumber, String cbnBankCode);


    @Query(value="SELECT a FROM Account a WHERE a.organization.id = ?1",
            countQuery = "SELECT COUNT(a) FROM Account a WHERE a.organization.id = ?1")
    Page<Account> findAllByOrganization(long orgId, Pageable pageable);

    @Query(value="SELECT a FROM Account a WHERE a.accountName IS NULL OR a.accountName = 'X' ORDER BY a.id DESC",
    countQuery = "SELECT COUNT(a.id) FROM Account a WHERE a.accountName IS NULL OR a.accountName = 'X' ORDER BY a.id DESC")
    Page<Account> findWithInvalidNames(Pageable pageable);

    @Query(value = "SELECT a FROM Account a WHERE a.mandateStatus IS NULL OR a.mandateStatus NOT IN ('00','26') AND a.accountStatus = ?1 ORDER BY a.id DESC",
    countQuery = "SELECT COUNT(a.id) FROM Account a WHERE a.mandateStatus IS NULL OR a.mandateStatus NOT IN ('00','26') AND a.accountStatus = ?1 ORDER BY a.id DESC")
    Page<Account> findWithInvalidMandates(AccountStatus accountStatus,Pageable pageable);


    @Query("SELECT a FROM Account a JOIN FETCH a.organization WHERE a.accountStatus = ?1 AND a.expiryDate <= ?2 and a.paymentMode = ?3")
    List<Account> findForPaymentNotification(AccountStatus accountStatus, Date date, PaymentMode paymentMode);


    @Transactional
    @Modifying
    @Query("UPDATE Account a SET a.accountStatus = ?1 WHERE a.expiryDate <= CURRENT_DATE and a.paymentMode = ?2")
    void disableExpiredAccounts(AccountStatus newStatus, PaymentMode paymentMode);

//    @Query("SELECT a FROM Account a WHERE a.accountStatus = ?1 AND a.expiryDate <= CURRENT_DATE ")
//    List<Account> findForExpiration(AccountStatus accountStatus);

    @Query("SELECT a FROM Account a WHERE a.accountNumber = ?1 AND a.organization.id = ?2")
    List<Account> findByAccountNumberAndOrganization(String accountNumber, long orgId);

    @Query(value="SELECT new com.nibss.corporatelounge.dto.Account(a.accountNumber,a.accountName,a.bank.cbnCode,a.accountStatus) FROM Account a WHERE a.organization.id = ?1",
            countQuery = "SELECT COUNT(a) FROM Account a WHERE a.organization.id = ?1")
    Page<Account> findStrippedByOrganization(long orgId, Pageable pageable);

    @Query("SELECT new com.nibss.corporatelounge.dto.Account(a.accountNumber,a.accountName,a.bank.cbnCode,a.accountStatus) FROM Account a WHERE a.accountNumber = ?1 AND a.organization.id = ?2")
    List<Account> findStrippedByAccountNumberAndOrganization(String accountNumber, long id);

    @Query("select  new com.nibss.tqs.corporatelounge.ajax.AccountDto(a.id,a.accountName,a.accountNumber,a.accountStatus,a.bank.name,a.email,a.dateActive,a.expiryDate,a.paymentMode) FROM Account a where a.organization.id = ?1")
    Set<AccountDto> findAllByOrganizationForView(long orgId);

    @Transactional
    @Modifying
    @Query("update  Account a set a.email = ?2 where a.id = ?1")
    int updateAccountEmail(long acctId, String email);


    @Transactional
    @Modifying
    @Query("update Account a set a.accountStatus = ?2, a.dateActive = ?3, a.expiryDate = ?4 where  a.id = ?1")
    int updateAccountStatusAndDates(long acctId,AccountStatus accountStatus, Date dateActive, Date expiryDate);


    @Query("select a from Account a where a.accountNumber = ?1 and a.bank.cbnCode = ?2 and a.organization.id = ?3")
    Account findOrganizationAccount(String acctNumber, String cbnBankCode, long orgId);

    @Query("select a from Account a where a.dateActive between ?1 and ?2 and a.paymentMode = ?3")
    List<Account> findForBilling(Date startDate, Date endDate, PaymentMode annual);
}

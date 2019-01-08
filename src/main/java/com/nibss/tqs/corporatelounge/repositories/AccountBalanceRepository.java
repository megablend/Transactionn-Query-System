package com.nibss.tqs.corporatelounge.repositories;

import com.nibss.corporatelounge.dto.PaymentMode;
import com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nibss.corporatelounge.dto.AccountBalance;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 10/4/2017.
 */
public interface AccountBalanceRepository extends JpaRepository<AccountBalance,Long> {


    @Query("select new com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto(a.account.accountNumber," +
            "a.account.accountName,a.organization.name,a.account.bank.name,a.account.email,a.dateAdded)" +
            " from AccountBalance a where a.dateAdded between ?1 and ?2" )
    List<AccountBalanceDto> getRequestsForDuration(Date startDate, Date endDate);


    // TODO: 10/11/2017 write method that fetches acctBalances for specified date range and Payment mode type

    @Query("select a from AccountBalance a where a.dateAdded between ?1 and ?2 and a.account.paymentMode = ?3")
    List<AccountBalance> getForDurationAndPaymentMode(Date startDate, Date endDate, PaymentMode paymentMode);
}

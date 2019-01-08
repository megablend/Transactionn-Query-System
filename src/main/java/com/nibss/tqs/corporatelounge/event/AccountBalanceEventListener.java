package com.nibss.tqs.corporatelounge.event;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountBalance;
import com.nibss.tqs.corporatelounge.repositories.AccountBalanceRepository;
import com.nibss.tqs.corporatelounge.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Created by eoriarewo on 10/5/2017.
 */
@Component
@Slf4j
public class AccountBalanceEventListener {

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Autowired
    private AccountService accountService;


    @Async
    @TransactionalEventListener
    public void accountBalanceRequestHappened(AccountBalanceEvent event) {
        AccountBalance balance = event.getAccountBalance();
        try {
            Account account = accountService.findOrganizationAccount(balance.getAccount().getAccountNumber(),
                    balance.getAccount().getAccountBankCode(), balance.getOrganization().getId());
            if (null != account) {
                balance.setAccount(account);
                accountBalanceRepository.save(balance);
                log.trace("done saving BE request");

            } else
                log.warn("could not get acct for acct. no {} and org {}", balance.getAccount().getAccountNumber(),
                        balance.getOrganization().getId());
        } catch (Exception e) {
            log.error("could not save account balance request", e);
        }
    }
}

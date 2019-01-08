package com.nibss.tqs.corporatelounge.service;

import com.nibss.corporatelounge.dto.*;
import com.nibss.nip.dao.NipDAO;
import com.nibss.nip.dto.BalanceEnquiryRequest;
import com.nibss.nip.dto.BalanceEnquiryResponse;
import com.nibss.nip.dto.NESingleRequest;
import com.nibss.nip.dto.NESingleResponse;
import com.nibss.nip.util.NipResponseCodes;
import com.nibss.tqs.config.QueueConfig;
import com.nibss.tqs.corporatelounge.event.AccountBalanceEvent;
import com.nibss.tqs.corporatelounge.queue.AccountProfiling;
import com.nibss.tqs.corporatelounge.repositories.AccountBalanceRepository;
import com.nibss.tqs.ussd.query.MerchantBillPaymentQueryBuilder;
import com.nibss.tqs.ussd.query.MerchantPaymentQueryHelper;
import com.nibss.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Service
@Slf4j
public class ApiService {

    @Autowired
    private AccountService accountService;

    @Autowired
    @Qualifier("clBankService")
    private BankService bankService;

    @Autowired
    private ApplicationContext appContext;


    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${cl.nibss_nip_code}")
    private String nibssNipCode;

    @Value("${cl.accountPageSize}")
    private int accountPageSize;

    @Autowired
    @Qualifier("tqsExecutorService")
    private ExecutorService executorService;

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    public Response getAccounts(Organization organization, int pageNo, int pageSize) {
        synchronized (new Object()) {
            Response response = new Response();

            try {
                pageSize = pageSize > accountPageSize ? accountPageSize : pageSize;
                Page<Account> pgAcct = accountService.findStrippedByOrganization(organization.getId(), pageNo, pageSize);

                List<Account> accounts = pgAcct.getContent();

                if (null != accounts) {
                    response.setAccounts(accounts.toArray(new Account[0]));
                    response.setTotalAccounts(pgAcct.getTotalElements());
                    response.setTotalPages(pgAcct.getTotalPages());
                }

                response.setResponseDescription(NipResponseCodes.SUCCESSFUL.getResponseDesc());
                response.setResponseCode(NipResponseCodes.SUCCESSFUL.getResponseCode());

            } catch (Exception e) {
                log.error("could not get accounts for organization with code {}", organization.getInstitutionCode());
                response.setResponseCode(NipResponseCodes.SYSTEM_MALFUNCTION.getResponseCode());
                response.setResponseDescription(NipResponseCodes.SYSTEM_MALFUNCTION.getResponseDesc());
            }
            return response;
        }
    }


    public Response getAccount(String accountNumber, Organization organization) {
        synchronized (new Object()) {
            Response response = new Response();
            try {
                List<Account> accounts = accountService.findStrippedByAccountNumberAndOrganization(accountNumber, organization);

                if (null != accounts && !accounts.isEmpty()) {
                    response.setAccounts(accounts.toArray(new Account[0]));
                    response.setTotalAccounts(accounts.size());
                }
                response.setResponseCode(NipResponseCodes.SUCCESSFUL.getResponseCode());
                response.setResponseDescription(NipResponseCodes.SUCCESSFUL.getResponseDesc());

            } catch (Exception e) {
                log.error("could not get account", e);
                response.setResponseCode(NipResponseCodes.SYSTEM_MALFUNCTION.getResponseCode());
                response.setResponseDescription(NipResponseCodes.SYSTEM_MALFUNCTION.getResponseDesc());
            }

            return response;
        }
    }

    public Response getBalances(final Request request, Organization org) {

        synchronized (new Object()) {
            Response response = new Response();

            if (request.getAccounts() == null || request.getAccounts().length == 0) {
                response.setResponseCode(NipResponseCodes.FORMAT_ERROR.getResponseCode());
                response.setResponseDescription(NipResponseCodes.FORMAT_ERROR.getResponseDesc());
            } else if (request.getAccounts().length > org.getMaxRequestSize()) {
                response.setResponseCode("23");
                response.setResponseDescription(String.format("You cannot request balances for more than %d accounts", org.getMaxRequestSize()));
            } else {

                List<String> acctNos = Arrays.asList(request.getAccounts()).stream().map(a -> a.getAccountNumber()).collect(Collectors.toList());
                log.trace("no. of accts before filtering: {}", acctNos.size());
                Set<Account> orgAccts = accountService.findByStatusAndOrganization(AccountStatus.APPROVED, org.getId(), acctNos);

                response.setResponseCode(NipResponseCodes.SUCCESSFUL.getResponseCode());
                response.setResponseDescription(NipResponseCodes.SUCCESSFUL.getResponseDesc());

                if (null != orgAccts) {
                    orgAccts.retainAll(Arrays.asList(request.getAccounts()));
                    log.trace("no. of accts after filtering: {}", orgAccts.size());
                    //after fetching from DB, compare accounts to ensure equality: they have same bank codeList<Account> balanceAccts = Stream.of(request.getAccounts()).filter( a -> orgAccts.contains(a)).collect(Collectors.toList());

                    if (!orgAccts.isEmpty()) {
                        //publish events
                        Map<Account, String> acctMap = new ConcurrentHashMap<>();


                        List<Thread> threads = orgAccts.stream().map(a -> {
                            Runnable r = () -> {
                                String sessionId = SessionUtil.generateNewSessionID(nibssNipCode);
                                BalanceEnquiryResponse be = new NipHelper().getAccountBalance(sessionId, a);


                                a.setResponseCode(be.getResponseCode());
                                if (be.getResponseCode() != null && be.getResponseCode().equals(NipResponseCodes.SUCCESSFUL.getResponseCode())) {
                                    a.setBalance(be.getAvailableBalance());
                                } else if (null == be.getResponseCode())
                                    a.setResponseCode(NipResponseCodes.NO_ACTION_TAKEN.getResponseCode());

                                AccountBalance balance = new AccountBalance();
                                balance.setSessionId(sessionId);
                                balance.setAccount(a);
                                balance.setOrganization(org);
                                balance.setResponseCode(a.getResponseCode());

                                Account account = accountService.findOrganizationAccount(a.getAccountNumber(), a.getAccountBankCode(), org.getId());
                                if( null != account) {
                                    AccountBalance b = new AccountBalance();
                                    b.setAccount(account);
                                    b.setOrganization(org);
                                    b.setSessionId(sessionId);
                                    b.setResponseCode(balance.getResponseCode());

                                   try {
                                       accountBalanceRepository.save(b);
                                       log.trace("done saving BE request in DB");
                                   } catch (Exception e) {
                                       log.error("could not save BE request for acct no {}",a.getAccountNumber(),e);
                                   }
                                } else
                                    log.warn("could not get acct details from db for acct {} n org {}", a.getAccountNumber(), org.getId());
                            };

                            return r;
                        }).map(r -> new Thread(r)).map(t -> {
                            t.start();
                            return t;
                        }).collect(Collectors.toList());


                        threads.forEach(t -> {
                            try {
                                t.join();
                            } catch (InterruptedException e) {

                            }
                        });

                        response.setAccounts(orgAccts.toArray(new Account[0]));
                        response.setTotalAccounts(orgAccts.size());
                    }
                }
            }
            return response;
        }
    }

    public void addAccounts(final Organization org, Set<Account> accounts) {
        if (null == accounts || accounts.isEmpty())
            return;

        Runnable r = () -> {
            List<Account> successfulAccts = new ArrayList<>();
            List<Account> failedAccts = new ArrayList<>();

            for (Account a : accounts) {
                if (a.getAccountBankCode() != null) {
                    try {
                        Bank bank = bankService.findByCode(a.getAccountBankCode());
                        if (null != bank) {
                            a.setBank(bank);
                            try {
                                log.trace("scanning for account {} {}", a.getAccountNumber(), a.getAccountBankCode());
                                long count = accountService.countAccountByOrganization(org.getId(), a.getAccountNumber(), a.getAccountBankCode());
                                if (count > 0) {
                                    //acct has already been maintained for organization
                                    log.trace("account already exist. skipping");
                                    continue;
                                }
                            } catch (Exception e) {
                                log.error("could not get count for acct {}", a);
                            }
                            String acctName = new NipHelper().getAccountName(a);
                            if (null != acctName) {
                                a.setAccountName(acctName);
                                a.setOrganization(org);
                                a.setAccountStatus(AccountStatus.PENDING);
                                successfulAccts.add(a);

                            } else
                                failedAccts.add(a);

                        } else {
                            failedAccts.add(a);
                        }
                    } catch (Exception e) {
                        log.error("could not add acct with code {}", a);
                        failedAccts.add(a);

                    }
                }

            }

            AccountProfiling accountProfiling = new AccountProfiling();
            accountProfiling.setSuccessful(successfulAccts.toArray(new Account[0]));
            accountProfiling.setFailed(failedAccts.toArray(new Account[0]));
            accountProfiling.setOrganization(org);

            if (!successfulAccts.isEmpty()) {
                for (Account a : successfulAccts)
                    try {
                        accountService.save(a);
                    } catch (Exception e) {
                        log.error("could not persist acct {}", a, e);
                    }
            }
            jmsTemplate.convertAndSend(QueueConfig.CL_ACCOUNT_PROFILING_QUEUE, accountProfiling);
            log.trace("done adding accounts from runnable");

        };

        executorService.execute(r);
    }

    public NipResponseCodes validateAccounts(Set<Account> accounts) {
        //check that bank codes were specified n has length of 3 characters
        //check that emails where specified
        //check tht account nos were specified n that each has a length of 10 characters
        //
        if (null == accounts || accounts.isEmpty()) {
            log.warn("account list is empty");
            return NipResponseCodes.FORMAT_ERROR;
        }

        if (accounts.stream().anyMatch(a -> a.getAccountBankCode() == null || a.getAccountBankCode().length() != 3)) {
            log.warn("bank code for specified account has an entry with > 3 characters");
            return NipResponseCodes.FORMAT_ERROR;
        }

        if (accounts.stream().anyMatch(a -> a.getAccountNumber() == null || a.getAccountNumber().length() != 10)) {
            log.warn("acct number length != 10");
            return NipResponseCodes.FORMAT_ERROR;
        }

        if (accounts.stream().anyMatch(a -> a.getEmail() == null || !MerchantPaymentQueryHelper.EMAIL_PATTERN.matcher(a.getEmail()).matches())) {
            log.warn("email empty or is invalid");
            return NipResponseCodes.FORMAT_ERROR;
        }

        return NipResponseCodes.SUCCESSFUL;

    }


    class NipHelper {
        public BalanceEnquiryResponse getAccountBalance(final Account account) {
            BalanceEnquiryRequest be = new BalanceEnquiryRequest();
            be.setAuthorizationCode(account.getMandateReference());
            be.setChannelCode(1);
            be.setDestinationInstitutionCode(bankService.findNipCodeFromCbnCode(account.getAccountBankCode()));
            be.setTargetAccountName(account.getAccountName());
            be.setTargetAccountNumber(account.getAccountNumber());
            be.setSessionID(SessionUtil.generateNewSessionID(nibssNipCode));

            return getBalanceEnquiryResponse(be);
        }

        public BalanceEnquiryResponse getAccountBalance(String sessionId, final Account account) {
            BalanceEnquiryRequest be = new BalanceEnquiryRequest();
            be.setAuthorizationCode(account.getMandateReference());
            be.setChannelCode(1);
            be.setDestinationInstitutionCode(bankService.findNipCodeFromCbnCode(account.getAccountBankCode()));
            be.setTargetAccountName(account.getAccountName());
            be.setTargetAccountNumber(account.getAccountNumber());
            be.setSessionID(sessionId);

            return getBalanceEnquiryResponse(be);
        }

        private BalanceEnquiryResponse getBalanceEnquiryResponse(BalanceEnquiryRequest be) {
            try {
                NipDAO nipDAO = appContext.getBean(NipDAO.class);
                BalanceEnquiryResponse response = nipDAO.sendBalanceEnquiry(be);
                return response;
            } catch (Exception e) {
                log.error("could not get balance enquiry response", e);
                BalanceEnquiryResponse err = new BalanceEnquiryResponse();
                err.setResponseCode(NipResponseCodes.SYSTEM_MALFUNCTION.getResponseCode());
                return err;
            }
        }

        public String getAccountName(final Account account) {
            NESingleRequest request = new NESingleRequest();
            request.setSessionID(SessionUtil.generateNewSessionID(nibssNipCode));
            request.setDestinationInstitutionCode(account.getBank().getNipCode());
            request.setAccountNumber(account.getAccountNumber());
            request.setChannelCode(1);

            try {
                NipDAO dao = appContext.getBean(NipDAO.class);
                NESingleResponse res = dao.sendNameEnquiry(request);
                log.trace("NE response code: {}", res.getResponseCode());
                if (res.getResponseCode() != null) {
                    if (res.getResponseCode().equals(NipResponseCodes.SUCCESSFUL.getResponseCode()))
                        return res.getAccountName();
                    if (res.getResponseCode().equals(NipResponseCodes.INVALID_ACCOUNT.getResponseCode()) || res.getResponseCode().equals(NipResponseCodes.DORMANT_ACCOUNT)
                            )
                        return null;
                    else
                        return "X";

                } else
                    return null;

            } catch (Exception e) {
                log.error("could not get account name for account {}", account, e);
                return null;
            }
        }
    }
}

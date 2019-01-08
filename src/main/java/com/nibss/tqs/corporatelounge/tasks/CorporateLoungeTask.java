package com.nibss.tqs.corporatelounge.tasks;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountStatus;
import com.nibss.corporatelounge.dto.PaymentMode;
import com.nibss.nip.dao.NipDAO;
import com.nibss.nip.dto.MandateAdviceRequest;
import com.nibss.nip.dto.MandateAdviceResponse;
import com.nibss.nip.dto.NESingleRequest;
import com.nibss.nip.dto.NESingleResponse;
import com.nibss.nip.util.NipResponseCodes;
import com.nibss.tqs.config.QueueConfig;
import com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto;
import com.nibss.tqs.corporatelounge.ajax.BalanceEnquiryReportDto;
import com.nibss.tqs.corporatelounge.repositories.AccountBalanceRepository;
import com.nibss.tqs.corporatelounge.repositories.AccountRepository;
import com.nibss.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 5/4/2017.
 */
@Service
@Slf4j
public class CorporateLoungeTask {

    private final static BigDecimal AMOUNT = new BigDecimal("10000.00");


    @Autowired
    @Qualifier("clAcctRepo")
    private AccountRepository accountRepository;

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Value("${cl.nibssNipCode}")
    private String nibssNipCode;

    @Autowired
    private NipDAO nipDAO;

    @Autowired
    private JmsTemplate template;
    /*
    tasks to be registered.

    1. get accounts with invalid account names. Do NE on these accts
    2. get accounts with invalid mandate response codes. Do MA on these
    3. get active accounts that will be due for payment soon. Send mail notification
    4. get accounts that have expired. Update statuses to EXPIRED
     */


    /*
   2. process accounts with invalid mandate status
    */
    @Transactional
    @Scheduled(cron = "${cl.invalid_mandate_cron}")
    public void processInvalidMandateAccounts() {
        Page<Account> accounts = null;
        try {
            Pageable pageable = new PageRequest(0, 20);
            accounts = accountRepository.findWithInvalidMandates(AccountStatus.APPROVED,pageable);
        } catch (Exception e) {
            log.error("could not get invalid mandate accts",e);
        }

        if( null == accounts || accounts.getNumberOfElements() == 0)
            return;

        List<Account> accountsContent = accounts.getContent();
        if( null == accountsContent || accountsContent.isEmpty())
            return;

        for(Account acct : accountsContent) {
            doMandateAdvice(acct);
            try {
                accountRepository.save(acct);
            } catch (Exception e) {}
        }

    }


    /*
   1. invalid acct names
    */
    @Transactional
    @Scheduled(cron = "${cl.invalid_acct_names_cron}")
    public void processInvalidAccountNames() {
        Page<Account> accountPage = null;
        try {
            accountPage = accountRepository.findWithInvalidNames( new PageRequest(0, 100));
        } catch (Exception e) {
            log.error("could not get accounts with invalid names",e);
        }

        if( null == accountPage || accountPage.getNumberOfElements() == 0)
            return;

        List<Account> accounts = accountPage.getContent();
        if( null == accounts || accounts.isEmpty())
            return;

        for(Account acct : accounts)
            doNameEnquiry(acct);

    }

    /*
    3. accounts due for payment
     */
    @Transactional(readOnly = true)
    @Scheduled(cron = "${cl.payment_notification_cron_week}")
    public void notifyOfPayment_week() {

        LocalDate oneWeekFromNow = LocalDate.now().plusWeeks(1);
        Date oneWeek = java.sql.Timestamp.valueOf(oneWeekFromNow.atStartOfDay() );

        sendForPaymentNotification(oneWeek);
    }


    @Transactional(readOnly = true)
    @Scheduled(cron = "${cl.payment_notification_cron_month}")
    public void notifyOfPayment_month() {

        LocalDate oneMonthFromNow = LocalDate.now().plusMonths(1);
        Date oneMonth = java.sql.Timestamp.valueOf(oneMonthFromNow.atStartOfDay() );

        sendForPaymentNotification(oneMonth);
    }

    @Scheduled(cron = "${cl.balance_request_notification_cron}")
    public void sendBalanceRequestNotifications() {
        Date oneWeekAgo = Timestamp.valueOf(LocalDate.now().minusDays(7).atStartOfDay());
        Date yesterday = Timestamp.valueOf(LocalDate.now().minusDays(1).atTime(23,59,59));

        try {
            List<AccountBalanceDto> dtos = accountBalanceRepository.getRequestsForDuration(oneWeekAgo, yesterday);
            if( null == dtos || dtos.isEmpty())
                return;
            // TODO: 10/5/2017 group dtos by email n then send each grouping to queue for sending email reports
            log.trace("no of items for BE notification mail: {}", dtos.size());
            template.convertAndSend(QueueConfig.CL_EMAIL_NOTIFICATION,
                    new BalanceEnquiryReportDto(oneWeekAgo, yesterday, dtos));
        } catch (Exception e) {
            log.error("could not get balance requests from db",e);
        }
    }

    private void sendForPaymentNotification(Date date) {
        List<Account> accounts = null;
        try {
            accounts = accountRepository.findForPaymentNotification(AccountStatus.APPROVED, date, PaymentMode.ANNUAL);
        } catch(Exception e) {
            log.error("could not get accts for payment notification",e);
        }

        if( null == accounts || accounts.isEmpty())
            return;


        template.convertAndSend(QueueConfig.CL_EXPIRING_ACCOUNTS, accounts.toArray(new Account[0]));
    }

    @Transactional
    @Scheduled(cron = "${cl.disable_accounts_cron}")
    public void disableExpiredAccounts() {
        try {
            accountRepository.disableExpiredAccounts(AccountStatus.EXPIRED, PaymentMode.ANNUAL);
        } catch (Exception e) {
            log.error("could not disable expired accounts",e);
        }
    }

    private void doMandateAdvice(final Account account) {
        if( null == account.getMandateReference() || account.getMandateReference().trim().isEmpty())
            account.setMandateReference(SessionUtil.generateRandomNumber(15));

        MandateAdviceRequest request = new MandateAdviceRequest();
        request.setAmount(AMOUNT );
        request.setBeneficiaryAccountName("Nigeria Inter-Bank Set. System Plc.");
        request.setBeneficiaryAccountNumber("0145782645");
        request.setChannelCode(1);
        request.setDebitAccountName(account.getAccountName());
        request.setDebitAccountNumber(account.getAccountNumber());
        request.setMandateReferenceNumber(account.getMandateReference());
        request.setDestinationInsitutionCode(account.getBank().getNipCode());
        request.setSessionId(SessionUtil.generateNewSessionID(nibssNipCode));


        try {
            MandateAdviceResponse response = nipDAO.sendMandateAdvice(request);
            if( null != response.getResponseCode() && !response.getResponseCode().trim().isEmpty())
                account.setMandateStatus(response.getResponseCode().trim());
            else
                account.setMandateStatus(NipResponseCodes.SYSTEM_MALFUNCTION.getResponseCode());

        } catch (Exception e) {
            log.error("error while sending mandate advice",e);
            account.setMandateStatus(NipResponseCodes.SYSTEM_MALFUNCTION.getResponseCode());
        }
    }

    private void doNameEnquiry(final Account account) {
        NESingleRequest request = new NESingleRequest();
        request.setChannelCode(1);
        request.setAccountNumber(account.getAccountNumber());
        request.setDestinationInstitutionCode(account.getBank().getNipCode());
        request.setSessionID(SessionUtil.generateNewSessionID(nibssNipCode));

        try {
            NESingleResponse response = nipDAO.sendNameEnquiry(request);
            if( null != response.getResponseCode()) {
                if( response.getResponseCode().trim().equals(NipResponseCodes.SUCCESSFUL.getResponseCode())) {
                    account.setAccountName(response.getAccountName());
                    accountRepository.save(account);
                } else if( response.getResponseCode().trim().equals(NipResponseCodes.INVALID_ACCOUNT.getResponseCode()))
                    accountRepository.delete(account);
            }
        } catch (Exception e) {
            log.error("could not get account name",e);
        }
    }
}

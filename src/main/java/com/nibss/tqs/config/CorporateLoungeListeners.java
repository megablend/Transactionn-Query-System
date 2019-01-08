package com.nibss.tqs.config;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.Organization;
import com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto;
import com.nibss.tqs.corporatelounge.ajax.AccountStatusChangeDto;
import com.nibss.tqs.corporatelounge.ajax.BalanceEnquiryReportDto;
import com.nibss.tqs.corporatelounge.queue.AccountProfiling;
import com.nibss.tqs.corporatelounge.reports.BalanceRequestReport;
import com.nibss.tqs.corporatelounge.reports.CustomByteArrayResource;
import com.nibss.tqs.corporatelounge.service.AccountService;
import com.nibss.tqs.corporatelounge.service.ClientService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by eoriarewo on 5/2/2017.
 */
@Component
@Slf4j
public class CorporateLoungeListeners {


    @Autowired
    private MailHelper mailHelper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BalanceRequestReport balanceRequestReport;

    @Autowired
    private ClientService clientService;


    @Value("#{'${cl.mailing_list}'.split(',') }")
    private List<String> mailingList;

    @Value("#{ T(java.nio.file.Paths).get('${cl.apiFile}')}")
    private Path apiDocumentation;

    @Autowired
    @Qualifier("freeMarkerConfig")
    private freemarker.template.Configuration fmConfig;

    private final static String CLIENT_CREATION = "clClient.html";
    private final static String STATUS_CHANGE = "clStatus.html";
    private final static String ACCOUNT_PROFILING = "clAccountProfiling.html";
    private final static String KEY_CHANGE = "clKeyChange.html";
    private final static String EXPIRY = "clExpiry.html";
    private final static String EMAIL_NOTIFICATION = "clNotification.html";

    @JmsListener(destination = QueueConfig.CL_NEW_CLIENT, concurrency = "1")
    public void sendClientCreationMail(Organization organization) {

        Map<String, Object> map = new HashMap<>();
        map.put("org", organization);

        String mailBody = getMailBody(CLIENT_CREATION, map);
        if (null != mailBody) {

            List<Resource> resources = null;
            if (Files.exists(apiDocumentation))
                resources = Collections.singletonList(new FileSystemResource(apiDocumentation.toFile()));
            MailQueueListeners.sendMail(mailHelper, "Corporate Lounge: Profile Created", mailBody, organization.getEmails(),
                    mailingList, resources);
        }

    }


    @JmsListener(destination = QueueConfig.CL_ACCOUNT_STATUS_CHANGE)
    public void sendAccountStatusChangeMail(AccountStatusChangeDto dto) {
        /*
        get accts with ID.
        send mail to organization stating new status
         */
        try {
            List<Long> ids = new ArrayList<>();
            for (long i : dto.getAcctIds())
                ids.add(i);
            List<Account> accounts = accountService.findAll(ids);
            if (null == accounts || accounts.isEmpty())
                return;
            Map<String, Object> map = new HashMap<>();
            map.put("status", dto.getStatus());
            map.put("accounts", accounts);
            String toMail = accounts.get(0).getOrganization().getEmails();

            String body = getMailBody(STATUS_CHANGE, map);
            if (null != body)
                MailQueueListeners.sendMail(mailHelper, "Corporate Lounge: Account Status Change",
                        body, toMail, mailingList, null);

        } catch (Exception e) {
            log.error("could not get accts from DB", e);
        }
    }

    @JmsListener(destination = QueueConfig.CL_ACCOUNT_PROFILING_QUEUE)
    public void processAccountProfiling(final AccountProfiling accountProfiling) {

        Map<String, Object> map = new HashMap<>();
        if (accountProfiling.getOrganization() == null) {
            log.warn("organization name is null");
            map.put("orgName", "Fake name");
        } else
            map.put("orgName", accountProfiling.getOrganization());
        map.put("successfulAccts", accountProfiling.getSuccessful());
        map.put("failedAccts", accountProfiling.getFailed());

        map.put("successfulLength", accountProfiling.getSuccessful().length);
        map.put("failedLength", accountProfiling.getFailed().length);

        String mailBody = getMailBody(ACCOUNT_PROFILING, map);

        if (null != mailBody)
            MailQueueListeners.sendMail(mailHelper, "Corporate Lounge: Account Profiling", mailBody,
                    accountProfiling.getOrganization().getEmails(), mailingList, null);

    }

    @JmsListener(destination = QueueConfig.CL_KEY_CHANGE)
    public void apiKeyChange(Organization org) {

        //doing this cos the object sent it does not contain all required fields
        Organization organization = clientService.findById(org.getId());

        Map<String, Object> map = new HashMap<>();
        map.put("org", organization);
        String mailBody = getMailBody(KEY_CHANGE, map);

        if (null != mailBody)
            MailQueueListeners.sendMail(mailHelper,
                    "Corporate Lounge: API Key Change", mailBody, organization.getEmails(), null);

    }


    @JmsListener(destination = QueueConfig.CL_EXPIRING_ACCOUNTS)
    public void sendPaymentNofication(Account[] expiringAccts) {
        Map<Organization, List<Account>> byOrg = Stream.of(expiringAccts).collect(Collectors.groupingBy(Account::getOrganization));

        byOrg.forEach((k, v) -> sendExpiryMail(k, v));

    }

    private void sendExpiryMail(Organization org, List<Account> accounts) {
        Map<String, Object> map = new HashMap<>();
        map.put("org", org);
        map.put("accounts", accounts);
        String mailBody = getMailBody(EXPIRY, map);

        if (null != mailBody)
            MailQueueListeners.sendMail(mailHelper, "Corporate Lounge: Profiled Account Expiry", mailBody, org.getEmails(),
                    mailingList, null);
    }


    @JmsListener(destination = QueueConfig.CL_EMAIL_NOTIFICATION)
    public void balanceRequestEmailNotification(BalanceEnquiryReportDto dto) {

        Map<String, List<AccountBalanceDto>> byEmail = dto.getAccounts().stream().collect(Collectors.groupingBy(a -> a.getEmail()));
        byEmail.forEach((k, v) -> sendBalanceEnquiryMail(k, v, dto.getStartDate(), dto.getEndDate()));
    }

    public void sendBalanceEnquiryMail(String email, List<AccountBalanceDto> accounts, Date startDate, Date endDate) {

        ByteArrayOutputStream report = null;
        try {
            report = balanceRequestReport.generateReport(accounts);
            log.trace("done generating BE pdf report for email sending");
        } catch (Exception e) {
            log.error("could not generate BE request report", e);
        }

        if (null == report)
            return;

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", startDate);
        map.put("endDate", endDate);

        String mailBody = getMailBody(EMAIL_NOTIFICATION, map);
        if (null == mailBody)
            return;


        DateFormat fmt = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        String description = String.format("BE_RequestReport_%s_%s.pdf",
                fmt.format(startDate), fmt.format(endDate));

        Resource resource = new CustomByteArrayResource(report.toByteArray(), description);



        MailQueueListeners.sendMail(mailHelper, "Corporate Lounge: Balance Enquiry Request Report",
                mailBody, email, mailingList, Arrays.asList(resource));

    }

    private String getMailBody(String templateName, Map<String, Object> map) {

        try {
            Template template = fmConfig.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(map, writer);
            return writer.toString();
        } catch (IOException e) {
            log.error("could not get mail template {}", templateName, e);

        } catch (TemplateException e) {
            log.error("could not get process template {}", templateName, e);
        }
        return null;
    }
}

package com.nibss.tqs.controllers;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.nibss.tqs.ajax.AjaxCpayAccountTransaction;
import com.nibss.tqs.ajax.AjaxCpayCardTransaction;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nibss.tqs.ajax.AjaxResponse;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
import com.nibss.tqs.centralpay.dto.CpayAccountSharingConfig;
import com.nibss.tqs.centralpay.dto.CpayMerchant;
import com.nibss.tqs.centralpay.queries.AggregatorCpayAccountQueryBuilder;
import com.nibss.tqs.centralpay.queries.AggregatorCpayCardQueryBuilder;
import com.nibss.tqs.centralpay.queries.BankCpayAccountQueryBuilder;
import com.nibss.tqs.centralpay.queries.BankCpayCardQueryBuilder;
import com.nibss.tqs.centralpay.queries.MerchantCpayAccountQueryBuilder;
import com.nibss.tqs.centralpay.queries.MerchantCpayCardQueryBuilder;
import com.nibss.tqs.centralpay.queries.NibssCpayAccountQueryBuilder;
import com.nibss.tqs.centralpay.queries.NibssCpayCardQueryBuilder;
import com.nibss.tqs.centralpay.renderer.AggregatorCpayAccountRenderer;
import com.nibss.tqs.centralpay.renderer.AggregatorCpayCardRenderer;
import com.nibss.tqs.centralpay.renderer.BankCpayAccountRenderer;
import com.nibss.tqs.centralpay.renderer.BankCpayCardRenderer;
import com.nibss.tqs.centralpay.renderer.CpayAccountsRenderer;
import com.nibss.tqs.centralpay.renderer.CpayCardRenderer;
import com.nibss.tqs.centralpay.renderer.MerchantCpayAccountRenderer;
import com.nibss.tqs.centralpay.renderer.MerchantCpayCardRenderer;
import com.nibss.tqs.centralpay.renderer.NibssCpayAccountRenderer;
import com.nibss.tqs.centralpay.renderer.NibssCpayCardRenderer;
import com.nibss.tqs.centralpay.repositories.AccountTransactionRepository;
import com.nibss.tqs.centralpay.repositories.CardTransactionRepository;
import com.nibss.tqs.centralpay.repositories.CpayMerchantRepository;
import com.nibss.tqs.centralpay.repositories.CpaySharingConfigRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Emor on 8/1/2016.
 */
@Controller
@RequestMapping("/centralpay")
@Slf4j
public class CentralPayController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CpayMerchantRepository merchantRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private CpaySharingConfigRepository sharingConfigRepository;


    @Autowired
    private OrganizationRepository orgRepo;

    public static final String CPAY_CARD_QUERY = "cpayCardCustomQuery";

    public static final String CPAY_ACCT_QUERY = "cpayAccountCustomeriDDWIUEIYE";

    @RequestMapping(value = "/card", method = RequestMethod.GET)
    public String cards(Model model, Authentication auth, @RequestParam(value = "bank", required = false) Integer bank) {

        User loggedInUser = (User) auth.getPrincipal();
        IOrganization userOrg = loggedInUser.getOrganizationInterface();

        CpayCardRenderer renderer = null;

        if (null != bank && userOrg.getOrganizationType() == OrganizationType.BANK_INT)
            renderer = applicationContext.getBean(AggregatorCpayCardRenderer.class);
        else
            renderer = getForOrganizationCard(userOrg);


        model.addAttribute("tableHeader", renderer.getTableHeader());

        if (userOrg.getOrganizationType() !=  OrganizationType.MERCHANT_INT)
            model.addAttribute("searchable", true);

        return "centralpay_cards/transactions";

    }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public String accounts(Model model, Authentication auth, @RequestParam(value = "bank", required = false) Integer bank) {
        if (auth.getPrincipal() == null)
            return "redirect:/login";

        User loggedInUser = (User) auth.getPrincipal();
        IOrganization userOrg = loggedInUser.getOrganizationInterface();

        CpayAccountsRenderer renderer = null;

        if (null != bank && userOrg.getOrganizationType() == OrganizationType.BANK_INT)
            renderer = applicationContext.getBean(AggregatorCpayAccountRenderer.class);
        else
            renderer = getForOrganizationAccount(userOrg);
        if (userOrg.getOrganizationType() != OrganizationType.MERCHANT_INT)
            model.addAttribute("searchable", true);

        model.addAttribute("tableHeader", renderer.getTableHeader());
        return "centralpay_accounts/transactions";

    }

    @RequestMapping(value = "/card/transactions", method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>> cardTransactions(HttpServletRequest request, Authentication auth) {
        if (auth.getPrincipal() == null) //session not valid
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User loggedInUser = (User) auth.getPrincipal();
        IOrganization userOrg = loggedInUser.getOrganizationInterface();

        User tempUser = loggedInUser;
        CpayCardRenderer renderer = null;
        QueryBuilder builder = null;

        String bank = request.getParameter("isBank");

        if (null != bank && userOrg.getOrganizationType() == OrganizationType.BANK_INT) {
            renderer = applicationContext.getBean(AggregatorCpayCardRenderer.class);
            builder = applicationContext.getBean(AggregatorCpayCardQueryBuilder.class);
            Aggregator aggr = new Aggregator(orgRepo.findOne(userOrg.getId()));
            tempUser.setOrganization(aggr);
        } else {
            renderer = getForOrganizationCard(userOrg);
            builder = getQueryBuilderForOrganization(userOrg, true);
        }


        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);
        QueryDTO countDTO = builder.countQuery(tempUser, request.getParameterMap());
        QueryDTO txnDTO = builder.transactionsQuery(tempUser, request.getParameterMap());

        try {
            long totalCount = cardTransactionRepository.totalCount(tempUser);
            long filteredCount = cardTransactionRepository.filteredCount(countDTO);
            List<AjaxCpayCardTransaction> trxns = cardTransactionRepository.findTransactions(txnDTO, 0, 0);
            trxns = trxns == null ? new ArrayList<>() : trxns;

            JQueryDataTableResponse<Object> dtResponse = renderer.render(trxns, dtRequest, tempUser);
            dtResponse.setRecordsFiltered(filteredCount);
            dtResponse.setRecordsTotal(totalCount);

            //store queryDTO for trxn in session. This will be needed for downloading report
            request.getSession().setAttribute(CPAY_CARD_QUERY, txnDTO);
            return new ResponseEntity<JQueryDataTableResponse<Object>>(dtResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("could not get cpay card transactions to render", e);
            return new ResponseEntity<JQueryDataTableResponse<Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/account/transactions", method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>> accountTransactions(HttpServletRequest request, Authentication auth) {
        if (auth.getPrincipal() == null) //session not valid
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User loggedInUser = (User) auth.getPrincipal();
        IOrganization userOrg = loggedInUser.getOrganizationInterface();

        User tempUser = loggedInUser;

        String bank = request.getParameter("isBank");

        CpayAccountsRenderer renderer = null;
        QueryBuilder builder = null;

        if (bank != null && userOrg.getOrganizationType() == OrganizationType.BANK_INT) {
            renderer = applicationContext.getBean(AggregatorCpayAccountRenderer.class);
            builder = applicationContext.getBean(AggregatorCpayAccountQueryBuilder.class);
            Aggregator aggr = new Aggregator(orgRepo.findOne(userOrg.getId()));
            tempUser.setOrganization(aggr);
        } else {
            renderer = getForOrganizationAccount(userOrg);
            builder = getQueryBuilderForOrganization(userOrg, false);
        }


        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);
        QueryDTO countDTO = builder.countQuery(tempUser, request.getParameterMap());
        QueryDTO txnDTO = builder.transactionsQuery(tempUser, request.getParameterMap());

        try {
            long totalCount = accountTransactionRepository.totalCount(tempUser);
            long filteredCount = accountTransactionRepository.filteredCount(countDTO);
            log.trace("about getting records from DB ");
            StopWatch timer = new StopWatch();
            timer.start();
            List<AjaxCpayAccountTransaction> trxns = accountTransactionRepository.findTransactions(txnDTO, 0, 0);
            timer.stop();
            log.trace("Done getting txns. time taken in ms: {}", timer.getTotalTimeMillis());
            trxns = trxns == null ? new ArrayList<>() : trxns;

            JQueryDataTableResponse<Object> dtResponse = renderer.render(trxns, dtRequest, tempUser);
            dtResponse.setRecordsFiltered(filteredCount);
            dtResponse.setRecordsTotal(totalCount);

            //store queryDTO for trxn in session. This will be needed for downloading report
            request.getSession().setAttribute(CPAY_ACCT_QUERY, txnDTO);
            return ResponseEntity.ok(dtResponse);
        } catch (Exception e) {
            log.error("could not get cpay acct transactions to render", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private QueryBuilder getQueryBuilderForOrganization(final IOrganization org, boolean isCard) {

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                if (isCard)
                    return applicationContext.getBean(AggregatorCpayCardQueryBuilder.class);
                else
                    return applicationContext.getBean(AggregatorCpayAccountQueryBuilder.class);
            case OrganizationType.BANK_INT:
                if (isCard)
                    return applicationContext.getBean(BankCpayCardQueryBuilder.class);
                else
                    return applicationContext.getBean(BankCpayAccountQueryBuilder.class);
            case OrganizationType.MERCHANT_INT:
            default:
                if (isCard)
                    return applicationContext.getBean(MerchantCpayCardQueryBuilder.class);
                else
                    return applicationContext.getBean(MerchantCpayAccountQueryBuilder.class);
            case OrganizationType.NIBSS_INT:
                if (isCard)
                    return applicationContext.getBean(NibssCpayCardQueryBuilder.class);
                else
                    return applicationContext.getBean(NibssCpayAccountQueryBuilder.class);
        }


    }

    private CpayCardRenderer getForOrganizationCard(final IOrganization org) {
        CpayCardRenderer renderer = null;

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                renderer = applicationContext.getBean(AggregatorCpayCardRenderer.class);
                break;
            case OrganizationType.BANK_INT:
                renderer = applicationContext.getBean(BankCpayCardRenderer.class);
                break;
            case OrganizationType.MERCHANT_INT:
            default:
                renderer = applicationContext.getBean(MerchantCpayCardRenderer.class);
                break;
            case OrganizationType.NIBSS_INT:
                renderer = applicationContext.getBean(NibssCpayCardRenderer.class);
                break;
        }

        return renderer;
    }

    private CpayAccountsRenderer getForOrganizationAccount(final IOrganization org) {
        CpayAccountsRenderer renderer = null;

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                renderer = applicationContext.getBean(AggregatorCpayAccountRenderer.class);
                break;
            case OrganizationType.BANK_INT:
                renderer = applicationContext.getBean(BankCpayAccountRenderer.class);
                break;
            case OrganizationType.MERCHANT_INT:
            default:
                renderer = applicationContext.getBean(MerchantCpayAccountRenderer.class);
                break;
            case OrganizationType.NIBSS_INT:
                renderer = applicationContext.getBean(NibssCpayAccountRenderer.class);
        }
        return renderer;
    }

    @RequestMapping("/billing")
    @RolesAllowed({Role.NIBSS_ADMIN})
    public String merchants(Model model) {

        try {

            model.addAttribute("merchants", merchantRepository.findAll());
            model.addAttribute("sharingConfig", sharingConfigRepository.findAll());
        } catch (Exception e) {
            log.error("could not get cpay merchants and/or billing config", e);
        }
        return "centralpay_accounts/list";
    }


    @RequestMapping(value = "/card/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadCardTransactions(Authentication auth, HttpSession session, @RequestParam("type") String type,
                                                           @RequestParam(value = "bank", required = false) String bank) {
        if (null == auth.getPrincipal())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        QueryDTO qdto = (QueryDTO) session.getAttribute(CPAY_CARD_QUERY);
        if (null == qdto)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        User user = (User) auth.getPrincipal();

        CpayCardRenderer renderer = getForOrganizationCard(user.getOrganizationInterface());
        if (bank != null && user.getOrganizationInterface().getOrganizationType() == OrganizationType.BANK_INT)
            renderer = applicationContext.getBean(AggregatorCpayCardRenderer.class);

        try {
            List<AjaxCpayCardTransaction> txns = cardTransactionRepository.findTransactions(qdto, 0, 0);
            if (null == txns || txns.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = String.format("%s_%s_%s", user.getOrganizationInterface().getName(), "CentralPayCard",
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            String contentType = "";
            DownloadType downloadType = DownloadType.CSV;

            switch (type.toLowerCase()) {
                case "csv":
                default:
                    downloadType = DownloadType.CSV;
                    fileName += ".csv";
                    contentType = DownloadType.CSV.getMimeType();
                    break;
                case "excel":
                    downloadType = DownloadType.EXCEL;
                    fileName += ".xlsx";
                    contentType = DownloadType.EXCEL.getMimeType();
                    break;
                case "pdf":
                    downloadType = DownloadType.PDF;
                    fileName += ".pdf";
                    contentType = DownloadType.PDF.getMimeType();
                    break;
            }

            httpHeaders.set("Content-Type", contentType);
            httpHeaders.set("Content-Disposition", String.format("inline;filename=\"%s\"", fileName));
            ByteArrayOutputStream out = renderer.download(downloadType, txns);
            return new ResponseEntity<>(out.toByteArray(), httpHeaders, HttpStatus.OK);

        } catch (Exception e) {
            log.error("could not download report", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/account/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadAccountTransactions(Authentication auth, HttpSession session, @RequestParam("type") String type,
                                                              @RequestParam(value = "bank", required = false) String bank) {
        if (null == auth.getPrincipal())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        QueryDTO qdto = (QueryDTO) session.getAttribute(CPAY_ACCT_QUERY);
        if (null == qdto)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        User user = (User) auth.getPrincipal();
        CpayAccountsRenderer renderer = getForOrganizationAccount(user.getOrganizationInterface());
        if (bank != null && user.getOrganizationInterface().getOrganizationType() == OrganizationType.BANK_INT)
            renderer = applicationContext.getBean(AggregatorCpayAccountRenderer.class);

        try {
            List<AjaxCpayAccountTransaction> txns = accountTransactionRepository.findTransactions(qdto, 0, 0);
            if (null == txns || txns.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = String.format("%s_%s_%s", user.getOrganizationInterface().getName(), "CentralPayAccount",
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            String contentType = "";
            DownloadType downloadType = DownloadType.CSV;

            switch (type.toLowerCase()) {
                case "csv":
                default:
                    downloadType = DownloadType.CSV;
                    fileName += ".csv";
                    contentType = DownloadType.CSV.getMimeType();
                    break;
                case "excel":
                    downloadType = DownloadType.EXCEL;
                    fileName += ".xlsx";
                    contentType = DownloadType.EXCEL.getMimeType();
                    break;
                case "pdf":
                    downloadType = DownloadType.PDF;
                    fileName += ".pdf";
                    contentType = DownloadType.PDF.getMimeType();
                    break;
            }

            httpHeaders.set("Content-Type", contentType);
            httpHeaders.set("Content-Disposition", String.format("inline;filename=\"%s\"", fileName));
            ByteArrayOutputStream out = renderer.download(downloadType, txns);
            return new ResponseEntity<>(out.toByteArray(), httpHeaders, HttpStatus.OK);

        } catch (Exception e) {
            log.error("could not download report", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/sharingconfig", method = RequestMethod.POST)
    @RolesAllowed({Role.NIBSS_ADMIN})
    public
    @ResponseBody
    AjaxResponse sharingConfiguration(@Valid CpayAccountSharingConfig config, BindingResult bR, Authentication auth) {

        if (null == auth.getPrincipal())
            return OrganizationController.buildFailedResponse(OrganizationController.SESSION_EXPIRED, null);

        if (bR.hasErrors()) {
            List<String> errors = bR.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
            OrganizationController.buildFailedResponse("Validation failed", errors);
        }

//        User loggedInUser = (User)auth.getPrincipal();

        CpayMerchant merchant = config.getMerchant();
        if (null == merchant)
            return OrganizationController.buildFailedResponse("No biller found for this Sharing Configuration", null);

        if (merchant.getSharingConfig() != null)
            return OrganizationController.buildFailedResponse("A sharing config has already been maintained for the merchant", null);

        merchant.setSharingConfig(config);

        try {
            merchantRepository.save(merchant);
            return OrganizationController.buildSuccessResponse("Sharing config was successfully saved");
        } catch (Exception e) {
            log.error("could not save Cpay sharing configuration", e);
            return OrganizationController.buildFailedResponse(OrganizationController.EXCEPTION_OCCURRED, null);
        }
    }
}

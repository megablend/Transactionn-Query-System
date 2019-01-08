package com.nibss.tqs.controllers;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.nibss.tqs.ajax.AjaxMcashTransaction;
import com.nibss.tqs.config.security.CurrentUser;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.merchantpayment.MerchantPaymentTransactionRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.ussd.query.AggregatorMerchantPaymentQueryBuilder;
import com.nibss.tqs.ussd.query.BankMerchantPaymentQueryBuilder;
import com.nibss.tqs.ussd.query.MerchantMerchantPaymentQueryBuilder;
import com.nibss.tqs.ussd.query.NibssMerchantPaymentQueryBuilder;
import com.nibss.tqs.ussd.renderer.AggregatorMerchantPayRenderer;
import com.nibss.tqs.ussd.renderer.BankMerchantPaymentRenderer;
import com.nibss.tqs.ussd.renderer.MerchantMerchantPaymentRenderer;
import com.nibss.tqs.ussd.renderer.MerchantPaymentRenderer;
import com.nibss.tqs.ussd.renderer.NibssMerchantPaymentRenderer;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by eoriarewo on 9/6/2016.
 */
@Controller
@RequestMapping("/merchantpay")
@Slf4j
public class MerchantPaymentController {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private MerchantPaymentTransactionRepository transactionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private final static String MERCHANTPAY_QUERY_DTO = "merchantPay7084004202842";


    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(@CurrentUser User user, Model model, @RequestParam(value = "bank", required = false) Integer bank) {

        MerchantPaymentRenderer renderer = null;
        if (null != bank && (user.getOrganizationInterface().getOrganizationType() == Integer.parseInt(OrganizationType.BANK)))
            renderer = appContext.getBean(AggregatorMerchantPayRenderer.class);
        else
            renderer = getRenderer(user.getOrganizationInterface());

        model.addAttribute("tableHeader", renderer.getTableHeader());
        model.addAttribute("searchable", true);

        return "ussd/merchantpay_transactions";

    }


    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>>
    transactions(HttpServletRequest request, @CurrentUser User user) {
        if (user == null) //session not valid
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User tempUser = user;

        String bank = request.getParameter("isBank");

        IOrganization userOrg = user.getOrganizationInterface();

        MerchantPaymentRenderer renderer = null;
        QueryBuilder builder = null;

        if (null != bank && (userOrg instanceof Bank)) {
            renderer = appContext.getBean(AggregatorMerchantPayRenderer.class);
            builder = appContext.getBean(AggregatorMerchantPaymentQueryBuilder.class);
            Aggregator aggr = new Aggregator(organizationRepository.findOne(userOrg.getId()));
            tempUser.setOrganization(aggr);
        } else if (userOrg instanceof Bank) {
            renderer = getRenderer(userOrg);
            //banks should only see mCash merchant transactions that have been mapped to them: aggregator like view
            builder = appContext.getBean(AggregatorMerchantPaymentQueryBuilder.class);
        } else {
            renderer = getRenderer(userOrg);
            builder = getQueryBuilder(userOrg);
        }


        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);
        QueryDTO countDTO = builder.countQuery(tempUser, request.getParameterMap());
        QueryDTO txnDTO = builder.transactionsQuery(tempUser, request.getParameterMap());

        try {
            long totalCount = transactionRepository.totalCount(tempUser);
            long filteredCount = transactionRepository.filteredCount(countDTO);
            List<AjaxMcashTransaction> trxns = transactionRepository.findTransactions(txnDTO, 0, 0);
            trxns = trxns == null ? new ArrayList<>() : trxns;
            log.trace("txns fetched from DB: {}", trxns.size());

            JQueryDataTableResponse<Object> dtResponse = renderer.render(trxns, dtRequest, tempUser);
            dtResponse.setRecordsFiltered(filteredCount);
            dtResponse.setRecordsTotal(totalCount);

            //store queryDTO for trxn in session. This will be needed for downloading report
            request.getSession().setAttribute(MERCHANTPAY_QUERY_DTO, txnDTO);
            return new ResponseEntity<JQueryDataTableResponse<Object>>(dtResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("could not get merchant payment transactions to render", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(Authentication auth, HttpSession session, @RequestParam("type") String type,
                                           @RequestParam(value = "bank", required = false) Integer bank) {
        if (auth.getPrincipal() == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        QueryDTO qDto = (QueryDTO) session.getAttribute(MERCHANTPAY_QUERY_DTO);
        if (null == qDto)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        User user = (User) auth.getPrincipal();
        MerchantPaymentRenderer renderer = null;
        if (bank != null && (user.getOrganizationInterface().getOrganizationType() == OrganizationType.BANK_INT) )
            renderer = appContext.getBean(AggregatorMerchantPayRenderer.class);
        else
            renderer = getRenderer(user.getOrganizationInterface());

        try {
            List<AjaxMcashTransaction> txns = transactionRepository.findTransactions(qDto, 0, 0);
            if (null == txns || txns.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = String.format("%s_%s_%s", user.getOrganizationInterface().getName(), "MerchantPayment",
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            String contentType;
            DownloadType downloadType;

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

    private MerchantPaymentRenderer getRenderer(final IOrganization org) {

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                return appContext.getBean(AggregatorMerchantPayRenderer.class);
            case OrganizationType.BANK_INT:
                return appContext.getBean(BankMerchantPaymentRenderer.class);
            default:
            case OrganizationType.MERCHANT_INT:
                return appContext.getBean(MerchantMerchantPaymentRenderer.class);
            case OrganizationType.NIBSS_INT:
                return appContext.getBean(NibssMerchantPaymentRenderer.class);
        }

    }


    private QueryBuilder getQueryBuilder(IOrganization org) {

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
            case OrganizationType.BANK_INT:
                return appContext.getBean(AggregatorMerchantPaymentQueryBuilder.class);
            /*case OrganizationType.BANK_INT:
                return appContext.getBean(BankMerchantPaymentQueryBuilder.class);*/
            case OrganizationType.MERCHANT_INT:
            default:
                return appContext.getBean(MerchantMerchantPaymentQueryBuilder.class);
            case OrganizationType.NIBSS_INT:
                return appContext.getBean(NibssMerchantPaymentQueryBuilder.class);
        }
    }

}

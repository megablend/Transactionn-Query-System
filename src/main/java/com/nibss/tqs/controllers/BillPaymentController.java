package com.nibss.tqs.controllers;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.ajax.AjaxUssdTransaction;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.ussd.dto.UssdTransaction;
import com.nibss.tqs.ussd.query.AggregatorBillPaymentQueryBuilder;
import com.nibss.tqs.ussd.query.BankBillPaymentQueryBuilder;
import com.nibss.tqs.ussd.query.MerchantBillPaymentQueryBuilder;
import com.nibss.tqs.ussd.query.NibssBillPaymentQueryBuilder;
import com.nibss.tqs.ussd.renderer.*;
import com.nibss.tqs.ussd.repositories.UssdTransactionRepository;
import lombok.extern.slf4j.Slf4j;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 9/6/2016.
 */
@Controller
@RequestMapping("/billpayment")
@Slf4j
public class BillPaymentController {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private UssdTransactionRepository ussdTransactionRepository;

    @Autowired
    private OrganizationRepository orgRepo;

    private static final String BILLPAYMENT_QUERY_DTO = "billPaymentQueryDtotyeieejwehbas";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Authentication auth, Model model, @RequestParam(value = "bank", required = false) Integer bank) {
        User user = (User) auth.getPrincipal();
        BillPaymentRenderer renderer = null;
        IOrganization userOrg = user.getOrganizationInterface();

        if (null != bank && (userOrg instanceof Bank))
            renderer = appContext.getBean(AggregatorBillPaymentRenderer.class);
        else
            renderer = getRenderer(user.getOrganizationInterface());

        model.addAttribute("tableHeader", renderer.getTableHeader());

        if (!(userOrg instanceof Merchant))
            model.addAttribute("searchable", true);

        return "ussd/billpayment_transactions";
    }


    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>>
    transactions(HttpServletRequest request, Authentication auth) {
        if (auth.getPrincipal() == null) //session not valid
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User loggedInUser = (User) auth.getPrincipal();
        User tempUser = loggedInUser;

        IOrganization userOrg = loggedInUser.getOrganizationInterface();


        String bank = request.getParameter("isBank");

        BillPaymentRenderer renderer;
        QueryBuilder builder = null;
        if (null != bank && userOrg instanceof Bank) {
            renderer = appContext.getBean(AggregatorBillPaymentRenderer.class);
            builder = appContext.getBean(AggregatorBillPaymentQueryBuilder.class);
            Aggregator aggr = new Aggregator(orgRepo.findOne(userOrg.getId()));
            tempUser.setOrganization(aggr);
        } else {
            renderer = getRenderer(userOrg);
            builder = getQueryBuilder(userOrg);
        }

        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);
        QueryDTO countDTO = builder.countQuery(tempUser, request.getParameterMap());
        QueryDTO txnDTO = builder.transactionsQuery(tempUser, request.getParameterMap());

        try {
            long totalCount = ussdTransactionRepository.totalCount(tempUser);
            long filteredCount = ussdTransactionRepository.filteredCount(countDTO);
            List<AjaxUssdTransaction> trxns = ussdTransactionRepository.findTransactions(txnDTO, 0, 0);
            trxns = trxns == null ? new ArrayList<>() : trxns;

            JQueryDataTableResponse<Object> dtResponse = renderer.render(trxns, dtRequest, tempUser);
            dtResponse.setRecordsFiltered(filteredCount);
            dtResponse.setRecordsTotal(totalCount);

            //store queryDTO for trxn in session. This will be needed for downloading report
            request.getSession().setAttribute(BILLPAYMENT_QUERY_DTO, txnDTO);
            return new ResponseEntity<JQueryDataTableResponse<Object>>(dtResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("could not get bill payment transactions to render", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(Authentication auth, HttpSession session, @RequestParam("type") String type,
                                           @RequestParam(value = "bank", required = false) String bank) {
        if (null == auth.getPrincipal())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        QueryDTO qdto = (QueryDTO) session.getAttribute(BILLPAYMENT_QUERY_DTO);
        if (null == qdto)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        User user = (User) auth.getPrincipal();
        BillPaymentRenderer renderer = null;
        if (null != bank && !bank.trim().isEmpty())
            renderer = appContext.getBean(AggregatorBillPaymentRenderer.class);
        else
            renderer = getRenderer(user.getOrganizationInterface());

        try {
            List<AjaxUssdTransaction> txns = ussdTransactionRepository.findTransactions(qdto, 0, 0);
            if (null == txns || txns.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = String.format("%s_%s_%s", user.getOrganizationInterface().getName(), "UssdBillPayment",
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

    private BillPaymentRenderer getRenderer(final IOrganization org) {

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                return appContext.getBean(AggregatorBillPaymentRenderer.class);
            case OrganizationType.BANK_INT:
                return appContext.getBean(BankBillPaymentRenderer.class);
            case OrganizationType.MERCHANT_INT:
            default:
                return appContext.getBean(MerchantBillPaymentRenderer.class);
            case OrganizationType.NIBSS_INT:
                return appContext.getBean(NibssBillPaymentRenderer.class);
        }
    }

    private QueryBuilder getQueryBuilder(IOrganization org) {

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                return appContext.getBean(AggregatorBillPaymentQueryBuilder.class);
            case OrganizationType.BANK_INT:
                return appContext.getBean(BankBillPaymentQueryBuilder.class);
            case OrganizationType.MERCHANT_INT:
            default:
                return appContext.getBean(MerchantBillPaymentQueryBuilder.class);
            case OrganizationType.NIBSS_INT:
                return appContext.getBean(NibssBillPaymentQueryBuilder.class);
        }
    }
}

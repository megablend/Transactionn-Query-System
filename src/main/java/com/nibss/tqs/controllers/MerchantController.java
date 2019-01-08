package com.nibss.tqs.controllers;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.config.security.CurrentUser;
import com.nibss.tqs.core.entities.Bank;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.OrganizationType;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.merchantpayment.MPMerchantRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.ussd.query.BankMerchantListQueryBuilder;
import com.nibss.tqs.ussd.query.NibssMerchantListQueryBuilder;
import com.nibss.tqs.ussd.renderer.BankMerchantListRenderer;
import com.nibss.tqs.ussd.renderer.MerchantListRenderer;
import com.nibss.tqs.ussd.renderer.NibssMerchantListRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.UUID;

/**
 * Created by eoriarewo on 4/6/2017.
 */
@Controller
@RequestMapping("/merchantlist")
@Slf4j
public class MerchantController {

    @Autowired
    private ApplicationContext appContext;


    @Autowired
    private MPMerchantRepository merchantRepository;

    private final static String MERCHANT_LIST = "merchantList" + UUID.randomUUID().toString();


    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(@CurrentUser User user, Model model) {

        MerchantListRenderer renderer = null;
        IOrganization org = user.getOrganizationInterface();
        try {

            if( org.getOrganizationType() == OrganizationType.NIBSS_INT)
                renderer = appContext.getBean(NibssMerchantListRenderer.class);
            else if( org.getOrganizationType() == OrganizationType.BANK_INT)
                renderer = appContext.getBean(BankMerchantListRenderer.class);
        } catch (Exception e) {
            log.error("could not get renderer for organization",e);
        }

        model.addAttribute("tableHeader", renderer == null? new ArrayList<>() : renderer.getTableHeader());
        return "ussd/merchant_list";
    }


    @RequestMapping(value = "",method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>>
    transactions(HttpServletRequest request, @CurrentUser User user) {
        if(user == null) //session not valid
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        IOrganization org = user.getOrganizationInterface();
        MerchantListRenderer renderer = null;

        QueryBuilder builder = null;

        if( org.getOrganizationType() == OrganizationType.NIBSS_INT) {
            renderer = appContext.getBean(NibssMerchantListRenderer.class);
            builder = appContext.getBean(NibssMerchantListQueryBuilder.class);
        } else if( org.getOrganizationType() == OrganizationType.BANK_INT) {
            renderer = appContext.getBean(BankMerchantListRenderer.class);
            builder = appContext.getBean(BankMerchantListQueryBuilder.class);
        }

        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);
        QueryDTO countDTO = builder.countQuery(user,request.getParameterMap());
        QueryDTO merchantDTO = builder.transactionsQuery(user, request.getParameterMap());

        try {
            long totalCount = merchantRepository.totalCount(user);
            long filteredCount = merchantRepository.filteredCount(countDTO);
            List<Merchant> trxns = merchantRepository.findMerchants(merchantDTO,0,0);
            trxns = trxns == null ? new ArrayList<>() : trxns;

            JQueryDataTableResponse<Object> dtResponse = renderer.render(trxns,dtRequest,user);
            dtResponse.setRecordsFiltered(filteredCount);
            dtResponse.setRecordsTotal(totalCount);

            //store queryDTO for trxn in session. This will be needed for downloading report
            request.getSession().setAttribute(MERCHANT_LIST,merchantDTO);
            return  new ResponseEntity<>(dtResponse,HttpStatus.OK);
        } catch (Exception e) {
            log.error("could not get merchant payment transactions to render",e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @RequestMapping(value="/download", method=RequestMethod.GET)
    public ResponseEntity<byte[]> download(@CurrentUser User user, HttpSession session, @RequestParam("type") String type) {
        if( user == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        QueryDTO qDto = (QueryDTO)session.getAttribute(MERCHANT_LIST);
        if( null == qDto)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        MerchantListRenderer renderer = null;
        IOrganization org = user.getOrganizationInterface();
        if(org.getOrganizationType() == OrganizationType.NIBSS_INT)
            renderer = appContext.getBean(NibssMerchantListRenderer.class);
        else if( org.getOrganizationType() == OrganizationType.BANK_INT)
            renderer = appContext.getBean(BankMerchantListRenderer.class);


        try {
            List<Merchant> merchants =merchantRepository.findMerchants(qDto, 0, 0);
            if( null == merchants || merchants.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = String.format("%s_%s_%s", user.getOrganizationInterface().getName(), "MerchantList",
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            String contentType = "";
            DownloadType downloadType = DownloadType.CSV;

            switch(type.toLowerCase()) {
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

            httpHeaders.set("Content-Type",contentType);
            httpHeaders.set("Content-Disposition", String.format("inline;filename=\"%s\"",fileName));
            ByteArrayOutputStream out = renderer.download(downloadType,merchants);
            return  new ResponseEntity<>(out.toByteArray(),httpHeaders,HttpStatus.OK);

        } catch(Exception e) {
            log.error("could not download report",e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

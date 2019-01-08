package com.nibss.tqs.controllers;

import com.nibss.tqs.ajax.AjaxEbillsPayTransaction;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.core.repositories.OrganizationSettingRepository;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ebillspay.dto.UserParam;
import com.nibss.tqs.ebillspay.queries.AggregatorEbillsPayQueryBuilder;
import com.nibss.tqs.ebillspay.queries.BankEbillsPayQueryBuilder;
import com.nibss.tqs.ebillspay.queries.MerchantEbillsPayQueryBuilder;
import com.nibss.tqs.ebillspay.queries.NibssEbillsPayQueryBuilder;
import com.nibss.tqs.ebillspay.renderer.AggregatorTransactionRenderer;
import com.nibss.tqs.ebillspay.renderer.BankTransactionRenderer;
import com.nibss.tqs.ebillspay.renderer.MerchantTransactionRenderer;
import com.nibss.tqs.ebillspay.renderer.NibssTransactionRenderer;
import com.nibss.tqs.ebillspay.repositories.EbillsTransactionRepository;
import com.nibss.tqs.ebillspay.repositories.UserParamRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import com.nibss.tqs.renderer.EbillsTransactionRenderer;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PathVariable;
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
 * Created by eoriarewo on 7/4/2016.
 */
@Controller
@RequestMapping("/ebillspay")
@Slf4j
public class EbillsPayController {

    private static final String EBILLS_QUERY_DTO = "ebillsPayQueryDTO";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationSettings appSettings;
    
    
    @Autowired
    private Environment env;

    @Autowired
    private OrganizationRepository orgRepo;


    @Autowired
    private OrganizationSettingRepository orgSettingRepo;

    @Autowired
    private EbillsTransactionRepository ebillsTransactionRepository;

    @Autowired
    private UserParamRepository userParamRepository;

    @RequestMapping(value = "",method = RequestMethod.GET)
    public String index(Authentication auth, Model model, @RequestParam(value = "bank", required = false) Integer bank) {

        User loggedInUser = (User)auth.getPrincipal();
        IOrganization userOrg = loggedInUser.getOrganizationInterface();


        EbillsTransactionRenderer ebillsRenderer = null;
        if( null != bank && (userOrg instanceof  Bank))
            ebillsRenderer= applicationContext.getBean(AggregatorTransactionRenderer.class);
        else
            ebillsRenderer = getForOrganization(userOrg);

        model.addAttribute("searchable",false);

        model.addAttribute("tableHeader",ebillsRenderer.getTableHeader());
        if( userOrg.getOrganizationType() == Integer.parseInt(OrganizationType.NIBSS)) {
            model.addAttribute("showDateInitiated",true);
            model.addAttribute("searchable",true);
        }
        else
            model.addAttribute("showDateInitiated",orgSettingRepo.findShowDateInitiatedByOrginzation(userOrg.getId()));


        if( userOrg instanceof Aggregator || userOrg instanceof Bank )
            model.addAttribute("searchable",true);

        return "ebillspay/transactions";
    }


    @RequestMapping(value = "/receipt/{sessionId}",method = RequestMethod.GET)
    public String receipt(@PathVariable("sessionId") String sessionId) {

        String receiptRequest = Utility.buildPaymentReceipt(sessionId);
        String encrypted = Utility.encryptAES(receiptRequest, appSettings.ebillspayAesKey());

        String externalUrl = appSettings.ebillsPayExternalReceiptUrl().replace("{encrypted}",encrypted);

        return String.format("redirect:%s",externalUrl);
    }
    
    
    @RequestMapping(value="/newreceipt/{sessionId}",method=RequestMethod.GET)
    public String newReceipt(@PathVariable String sessionId) {
         String receiptRequest = Utility.buildPaymentReceipt(sessionId);
         
          String encrypted = Utility.encryptAES(receiptRequest, env.getProperty("ebillspay_new.aes_key"));
          
          String externalUrl = String.format("%s%s",env.getProperty("ebillspay_new.receipt_url"), encrypted);
          
         return String.format("redirect:%s",externalUrl);
    }
    


    @RequestMapping(value = "/params/{sessionId}",method = RequestMethod.GET)
    public String transactionParams(@PathVariable("sessionId") String sessionId, Model model) {

        List<UserParam> params = new ArrayList<>();
        try {
            params = userParamRepository.findBySessionId(sessionId);
        } catch (Exception e) {
            log.error("could not get transaction with session Id: "+sessionId,e );
        }

        model.addAttribute("userParams",params);
        return "ebillspay/userParams";

    }

    @RequestMapping(value = "/transactions",method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>>
    transactions(HttpServletRequest request, Authentication auth) {
        if(auth.getPrincipal() == null) //session not valid
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User loggedInUser = (User)auth.getPrincipal();
        IOrganization userOrg = loggedInUser.getOrganizationInterface();

        String bank = request.getParameter("isBank");

        EbillsTransactionRenderer renderer = null;

        User tempUser = loggedInUser;

        QueryBuilder builder = null;
        if( null != bank) {
            renderer = applicationContext.getBean(AggregatorTransactionRenderer.class);
            builder = applicationContext.getBean(AggregatorEbillsPayQueryBuilder.class);
            Aggregator agg = new Aggregator(orgRepo.findOne(userOrg.getId()));
            tempUser.setOrganization(agg);
        } else {
            renderer = getForOrganization(userOrg);
            builder = getQueryBuilderForOrganization(userOrg);
        }


        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);
        if( null == dtRequest)
            log.warn("could not instantiate dataTable Request");

        QueryDTO countDTO = builder.countQuery(tempUser,request.getParameterMap());
        QueryDTO txnDTO = builder.transactionsQuery(tempUser, request.getParameterMap());

        try {

            StopWatch timer = new StopWatch();
            timer.start();
            long totalCount = ebillsTransactionRepository.totalCount(tempUser);
            timer.stop();
            log.trace("done running count query. Time taken in ms: {}", timer.getTotalTimeMillis());

            timer = new StopWatch();
            log.trace("about running filtered count query");
            timer.start();
            long filteredCount = ebillsTransactionRepository.filteredCount(countDTO);
            timer.stop();
            log.trace("done running filtered count query. Time taken in ms: {}", timer.getTotalTimeMillis());

            timer = new StopWatch();
            log.trace("about running transaction query");
            timer.start();
            List<AjaxEbillsPayTransaction> trxns = ebillsTransactionRepository.findTransactions(txnDTO,0,0);
            timer.stop();
            log.trace("done running transaction query. time taken in ms: {}", timer.getTotalTimeMillis());

            trxns = trxns == null ? new ArrayList<>(0) : trxns;

            JQueryDataTableResponse<Object> dtResponse = renderer.render(trxns,dtRequest,tempUser);
            dtResponse.setRecordsFiltered(filteredCount);
            dtResponse.setRecordsTotal(totalCount);

            //store queryDTO for trxn in session. This will be needed for downloading report
            log.trace("No. of trxns from database: {}",trxns.size());
            request.getSession().setAttribute(EBILLS_QUERY_DTO,txnDTO);
            return  new ResponseEntity<JQueryDataTableResponse<Object>>(dtResponse,HttpStatus.OK);
        } catch (Exception e) {
            log.error("could not get ebillspay transactions to render",e);
            return new ResponseEntity<JQueryDataTableResponse<Object>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private QueryBuilder getQueryBuilderForOrganization(final IOrganization org) {

        switch (Integer.toString(org.getOrganizationType())) {
            case OrganizationType.AGGREGATOR:
                return applicationContext.getBean(AggregatorEbillsPayQueryBuilder.class);
            case OrganizationType.BANK:
                return applicationContext.getBean(BankEbillsPayQueryBuilder.class);
            case OrganizationType.MERCHANT:
                default:
                return applicationContext.getBean(MerchantEbillsPayQueryBuilder.class);
            case OrganizationType.NIBSS:
                return applicationContext.getBean(NibssEbillsPayQueryBuilder.class);
        }
    }

    private EbillsTransactionRenderer getForOrganization(final IOrganization org) {

        switch (Integer.toString(org.getOrganizationType())) {
            case OrganizationType.AGGREGATOR:
                return  applicationContext.getBean(AggregatorTransactionRenderer.class);
            case OrganizationType.BANK:
                return applicationContext.getBean(BankTransactionRenderer.class);
            case OrganizationType.MERCHANT:
                default:
                return applicationContext.getBean(MerchantTransactionRenderer.class);
            case OrganizationType.NIBSS:
                return applicationContext.getBean(NibssTransactionRenderer.class);

        }
    }

    @RequestMapping(value = "/download",method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(Authentication auth, HttpSession session, @RequestParam("type") String type,
                                           @RequestParam(value = "bank", required = false) Integer bank) {
        if( auth.getPrincipal() == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User user = (User)auth.getPrincipal();


        EbillsTransactionRenderer renderer = null;

        if( null != bank)
            renderer = applicationContext.getBean(AggregatorTransactionRenderer.class);
        else
            renderer = getForOrganization(user.getOrganizationInterface());

        QueryDTO trxnDTO = (QueryDTO)session.getAttribute(EBILLS_QUERY_DTO);
        if( null == trxnDTO)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        try {
            List<AjaxEbillsPayTransaction> transactions = ebillsTransactionRepository.findTransactions(trxnDTO,0,0);
            if( null == transactions || transactions.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            log.trace("No. of transactions gotten: {}",transactions.size());
            HttpHeaders httpHeaders = new HttpHeaders();
            DownloadType dType = DownloadType.CSV;
            String fileName = String.format("%s_%s_%s", user.getOrganizationInterface().getName(), "eBillsPay", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            String contentType = "text/csv";
            switch (type.toLowerCase()) {
                case "pdf":
                    dType = DownloadType.PDF;
                    fileName += ".pdf";
                    contentType = DownloadType.PDF.getMimeType();
                    break;
                case "csv":
                    default:
                    dType = DownloadType.CSV;
                    fileName += ".csv";
                        contentType = DownloadType.CSV.getMimeType();
                    break;
                case "excel":
                    dType = DownloadType.EXCEL;
                    fileName += ".xlsx";
                    contentType = DownloadType.EXCEL.getMimeType();
                    break;
            }

            httpHeaders.set("Content-Type",contentType);
            httpHeaders.set("Content-Disposition", String.format("inline;filename=\"%s\"",fileName));
            ByteArrayOutputStream out = renderer.download(dType,transactions);
            return  new ResponseEntity<>(out.toByteArray(),httpHeaders,HttpStatus.OK);

        } catch (Exception e) {
            log.error("could not download report",e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}

package com.nibss.tqs.ebillspay.renderer;

import com.nibss.tqs.ajax.AjaxEbillsPayTransaction;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.core.repositories.OrganizationSettingRepository;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.UserParam;
import com.nibss.tqs.ebillspay.repositories.UserEnteredParamService;
import com.nibss.tqs.renderer.EbillsTransactionRenderer;
import com.nibss.tqs.renderer.TransactionSummary;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.DownloadType;
import com.nibss.tqs.report.MerchantTransactionReportDownloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class MerchantTransactionRenderer implements EbillsTransactionRenderer {

    @Autowired
    private ApplicationSettings applicationSettings;

    @Autowired
    private UserEnteredParamService paramService;

    @Autowired
    private MerchantTransactionReportDownloader reportDownloader;

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private OrganizationSettingRepository orgSetRepo;

    public MerchantTransactionRenderer() {
    }

    @Override
    public JQueryDataTableResponse<Object> render(List<AjaxEbillsPayTransaction> transactions, JQueryDataTableRequest request, User user) {
        JQueryDataTableResponse response = new JQueryDataTableResponse();
        response.setDraw(request.getDraw());
        List<String> headerParams = null;

        Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(user.getOrganizationInterface().getId());
        if( null == billerIds || billerIds.isEmpty())
            headerParams = new ArrayList<>(0);
        else
         headerParams = paramService.getParamNamesForBiller(billerIds.iterator().next());

        List<AjaxEbillsPayTransaction> limitTrxn = getLimitedTransactions(transactions,request);

        log.trace("No of trxns to be shown: {}",limitTrxn.size());
        response.setData( buildTransactionJson(limitTrxn,headerParams).toArray(new Object[0]));

        List<TransactionSummary> summaries = getSummary(transactions, true);
        Map<String,Object> map = new HashMap<>();
        map.put("showFee",true);
        map.put("summary",summaries);
        response.setExtras(map);

        return response;
    }

    @Override
    public ByteArrayOutputStream download(DownloadType downloadType, List<AjaxEbillsPayTransaction> transactions) throws IOException {
        try {
            return reportDownloader.generateEbillspayReport(transactions,downloadType);
        } catch (Exception e) {
            throw new IOException("could not generate report",e);
        }
    }

    @Override
    public List<String> getTableHeader() {

        User loggedInUser = getUser();


        List<String> headers = new ArrayList<>();
        headers.add("Transaction ID");
        headers.add("Product");
        headers.add("Source Bank");
        headers.add("Branch Code");
        headers.add("Destination Bank");
//        headers.add("Customer No"); // don't think it's necessary since all params will be rendered for merchant
        if( loggedInUser != null) {
            Boolean isAllowed = orgSetRepo.findShowDateInitiatedByOrginzation(loggedInUser.getOrganizationInterface().getId());
            if( isAllowed)
                headers.add("Date Initiated");
        }
        headers.add("Date Approved");
        headers.add("Amount (N)");
        headers.add("Transaction Fee (N)");
        headers.add( "Total (N)");


        //add user entered params as column headers
        if( loggedInUser != null) {
            Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(loggedInUser.getOrganizationInterface().getId());
            if( null != billerIds && !billerIds.isEmpty()) {
                List<String> billerParams = paramService.getParamNamesForBiller(billerIds.iterator().next());
                headers.addAll(billerParams);
            }

        }

        headers.add("Status");
        headers.add(""); //for receipt link

        return headers;
    }

    private List<Object[]> buildTransactionJson(final List<AjaxEbillsPayTransaction> transactions, List<String> paramNames) {


        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN,Locale.ENGLISH);
        NumberFormat numberFormat = new DecimalFormat(NUMBER_FORMAT_PATTERN);

        List<Object[]> values = new ArrayList<>();

        User loggedInUser = getUser();
        Boolean viewDateInitiated = false;
        if( loggedInUser != null) {
            viewDateInitiated = orgSetRepo.findShowDateInitiatedByOrginzation(loggedInUser.getOrganizationInterface().getId());
        }

        boolean shouldView = viewDateInitiated;
        transactions.forEach( t -> {

            List<Object> temp = new ArrayList<>();
            temp.add(t.getSessionId());
            temp.add(t.getProductName());
            temp.add(t.getSourceBankName() == null ? "" : t.getSourceBankName());
            temp.add(t.getBranchCode());
            temp.add(t.getDestinationBankName() == null ? "" : t.getDestinationBankName());

            if( shouldView)
                temp.add( t.getTransactionDate() != null ? dateFormat.format(t.getTransactionDate()) : "");

            temp.add( t.getDateApproved() != null ? dateFormat.format(t.getDateApproved()) : "");
            temp.add( numberFormat.format(t.getAmount()));
            temp.add( numberFormat.format(t.getFee()));
            temp.add( numberFormat.format(t.getAmount().add(t.getFee())));

            List<UserParam> lstParam = null;


            try {
                lstParam = paramService.findBySessionId(t.getSessionId());
            } catch(Exception e) {}

            List<UserParam> params = lstParam;

            if( null == params || params.isEmpty()) {
                paramNames.forEach( s -> temp.add(""));
            } else {
                paramNames.forEach( s -> {
                    String value = getParamValueByName(params, s);
                    temp.add(value);
                });
            }

            temp.add( t.getResponseDescription());
            if( t.getResponseCode().equals(SUCCCESS_STATUS))
                temp.add( applicationSettings.ebillspayReceiptUrl().replace("sessionId",t.getSessionId()) );
            else
                temp.add("");

            values.add(temp.toArray(new Object[0]));

        });

        log.trace("Size of data array: {}", values.size());

        return values;
    }

    private String getParamValueByName(List<UserParam> params, String paramName) {
        if( null == params || params.isEmpty())
            return "";
        if( null == paramName || paramName.isEmpty())
            return "";
        UserParam param = params.stream().filter( p -> p.getName().equals(paramName)).findFirst().orElse(new UserParam());
        return param.getValue() == null ? "" : param.getValue();
    }


    private User getUser() {
        User user = null;
        SecurityContext holder = SecurityContextHolder.getContext();
        Authentication authentication = holder.getAuthentication();

        if( authentication != null) {
            user  = (User)authentication.getPrincipal();
        }
        return user;
    }
}

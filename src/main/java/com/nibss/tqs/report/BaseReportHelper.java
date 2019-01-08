package com.nibss.tqs.report;

import com.nibss.tqs.core.entities.Aggregator;
import com.nibss.tqs.core.entities.Bank;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.repositories.OrganizationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by eoriarewo on 9/8/2016.
 */
public class BaseReportHelper {

    public final static String NIBSS_CODE = "999";

    protected void sumCommission(Map<String,List<TransactionReport>> rptMap, String key, TransactionReport rpt) {

        List<TransactionReport> items = rptMap.get(key);
        items = items == null ? new ArrayList<>() : items;

        TransactionReport temp = items.stream().filter( t -> t.getSessionId().equals(rpt.getSessionId())).findFirst().orElse(null);
        if( null != temp) {
            BigDecimal sum = temp.getCalculatedAmount().add( rpt.getCalculatedAmount());
            temp.setCalculatedAmount(sum.setScale(2, RoundingMode.HALF_DOWN));
        } else
            items.add(rpt);

        rptMap.put(key,items);

    }

    protected String getAggregatorCodeForMerchantPay(long merchantId, final OrganizationRepository organizationRepository) {
        List<Organization> orgs = organizationRepository.findAggregatorForMerchantPaymentMerchant(merchantId);
        if( null == orgs || orgs.isEmpty())
            return null;

        Organization org = orgs.get(0);
        if( org instanceof Bank)
            return ((Bank)org).getCbnBankCode();
        else if(org instanceof Aggregator)
            return  ((Aggregator)org).getCode();

        return null; //shouldn't get here
    }

    protected String getAggregatorCodeForEbillsPay(int billerId, final OrganizationRepository organizationRepository) {
        List<Organization> orgs = organizationRepository.findAggregatorForEbillsPayBiller(billerId);
        if( null == orgs || orgs.isEmpty())
            return null;

        Organization org = orgs.get(0);
        if( org instanceof Bank)
            return ((Bank)org).getCbnBankCode();
        else if(org instanceof Aggregator)
            return  ((Aggregator)org).getCode();

        return null; //shouldn't get here
    }

    protected String getAggregatorCodeForCentralPay(String merchantCode, final OrganizationRepository organizationRepository) {
        List<Organization> orgs = organizationRepository.findAggregatorForCentralPayMerchant(merchantCode);
        if( null == orgs || orgs.isEmpty())
            return null;

        Organization org = orgs.get(0);
        if( org instanceof Bank)
            return ((Bank)org).getCbnBankCode();
        else if(org instanceof Aggregator)
            return  ((Aggregator)org).getCode();

        return null; //shouldn't get here
    }


    protected  String getAggregatorForUssdBiller(String merchantCode, final OrganizationRepository organizationRepository) {
        List<Organization> orgs = organizationRepository.findAggregatorForUssdBiller(merchantCode);
        if( null == orgs || orgs.isEmpty())
            return null;

        Organization org = orgs.get(0);
        if( org instanceof Bank)
            return ((Bank)org).getCbnBankCode();
        else if(org instanceof Aggregator)
            return  ((Aggregator)org).getCode();

        return null; //shouldn't get here
    }


    protected  boolean isEmptyBigDecimal(final BigDecimal stuff) {
        return  stuff == null || stuff.compareTo(BigDecimal.ZERO) <= 0;
    }
}

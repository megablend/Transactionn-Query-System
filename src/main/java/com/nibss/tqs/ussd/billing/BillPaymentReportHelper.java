package com.nibss.tqs.ussd.billing;

import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.report.BaseReportHelper;
import com.nibss.tqs.report.BillingReportGenerator;
import com.nibss.tqs.report.TransactionReport;
import com.nibss.tqs.ussd.dto.UssdBiller;
import com.nibss.tqs.ussd.dto.UssdFeeSharingConfig;
import com.nibss.tqs.ussd.dto.UssdTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 9/8/2016.
 */
@Component
@Scope("prototype")
public class BillPaymentReportHelper  extends BaseReportHelper implements Serializable {

    @Autowired
    private BillingReportGenerator billingReportGenerator;

    @Autowired
    private OrganizationRepository organizationRepository;



    public void generateReports(List<UssdTransaction> transactions) throws Exception {

        if( null == transactions || transactions.isEmpty())
            return;

        List<UssdTransaction> items = new ArrayList<>(transactions);

       //group by ussd biller
        Map<UssdBiller, List<UssdTransaction>> byMerchant = items.stream().collect(Collectors.groupingBy( t -> t.getUssdBiller()));

        Map<String,List<TransactionReport> > reportMap = new HashMap<>();

        byMerchant.forEach( (k,v) -> {
            UssdFeeSharingConfig config = k.getFeeSharingConfig();
            v.forEach( t -> {
                doAggregatorShare(t,config,reportMap);
                doBillerBankShare(t,config,reportMap);
                doCollectingBankShare(t,config,reportMap);
                doNibssShare(t,config,reportMap);
                doTelcoShare(t,config,reportMap);
                doUssdAggShare(t,config,reportMap);
            });
        });


        billingReportGenerator.generateReports(reportMap, Product.USSD_BILL_PAYMENT);

    }

    private void doTelcoShare(UssdTransaction t, UssdFeeSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if( isEmptyBigDecimal(config.getTelcoShare()))
            return;
        String telcoCode = t.getTelco().getId();
        BigDecimal telcoShare = config.isPercentage() ? config.getTelcoShare().multiply(t.getTransactionFee()) : config.getTelcoShare();
        TransactionReport trxn = new TransactionReport(t);
        trxn.setCalculatedAmount(telcoShare.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,telcoCode,trxn);
    }

    private void doCollectingBankShare(UssdTransaction t, UssdFeeSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(isEmptyBigDecimal(config.getCollectingBankShare()))
            return;

        String collectingBankCode = t.getSourceBankCode();
        BigDecimal colBnkShare = config.isPercentage() ? config.getCollectingBankShare().max(t.getTransactionFee()) : config.getCollectingBankShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(colBnkShare.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,collectingBankCode,r);
    }

    private void doUssdAggShare(UssdTransaction t,UssdFeeSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(isEmptyBigDecimal(config.getUssdAggregatorShare()))
            return;

        String ussdAggCode = t.getUssdAggregator().getId();
        BigDecimal share = config.isPercentage() ? config.getUssdAggregatorShare().multiply(t.getTransactionFee()) : config.getUssdAggregatorShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,ussdAggCode,r);
    }

    private void doAggregatorShare( UssdTransaction t, UssdFeeSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(isEmptyBigDecimal(config.getAggregatorShare()))
            return;
        String aggCode = getAggregatorForUssdBiller(t.getUssdBiller().getMerchantCode(),organizationRepository);
        if( null == aggCode)
            return;
        BigDecimal share = config.isPercentage() ? config.getAggregatorShare().multiply(t.getTransactionFee()) : config.getAggregatorShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap, aggCode,r);
    }

    private void doBillerBankShare(UssdTransaction t,UssdFeeSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(isEmptyBigDecimal(config.getBillerBankShare()) || config.getBillerBankCode() == null)
            return;

        BigDecimal share = config.isPercentage() ? config.getBillerBankShare().multiply(t.getTransactionFee()) : config.getBillerBankShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,config.getBillerBankCode(),r);

    }

    private void doNibssShare(UssdTransaction t, UssdFeeSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(isEmptyBigDecimal(config.getNibssShare()))
            return;

        BigDecimal share = config.isPercentage() ? config.getNibssShare().multiply(t.getTransactionFee()) : config.getNibssShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,NIBSS_CODE,r);
    }
}

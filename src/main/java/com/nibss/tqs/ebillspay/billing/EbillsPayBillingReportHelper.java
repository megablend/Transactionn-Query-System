package com.nibss.tqs.ebillspay.billing;

import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ebillspay.dto.EbillsBillingConfiguration;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.report.BaseReportHelper;
import com.nibss.tqs.report.BillingReportGenerator;
import com.nibss.tqs.report.TransactionReport;
import com.nibss.tqs.util.Utility;
import org.apache.activemq.store.kahadb.TransactionIdTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 9/8/2016.
 */
@Component
@Scope("prototype")
public class EbillsPayBillingReportHelper extends BaseReportHelper {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BillingReportGenerator billingReportGenerator;

    public void generateReports(List<BaseTransaction> transactions) throws Exception {
        Map<String,List<TransactionReport>> reportMap = new HashMap<>();


        Map<Biller,List<BaseTransaction>> byBiller = transactions.stream().collect(Collectors.groupingBy( t -> t.getBiller()));

        byBiller.forEach((k,v) -> {

            EbillsBillingConfiguration config = k.getEbillsBillingConfigurations();
            v.forEach( t -> {
                doAggregatorShare(t,config,reportMap);
                doBillerBankShare(t,config,reportMap);
                doCollectingBankShare(t,config,reportMap);
                doNibssShare(t,config,reportMap);
            });
        });


        billingReportGenerator.generateReports(reportMap, Product.EBILLSPAY);
    }


    private void doCollectingBankShare(BaseTransaction t, EbillsBillingConfiguration config, Map<String,List<TransactionReport>> rptMap) {
        if(Utility.isEmptyBigDecimal(config.getCollectingBankShare()))
            return;
        String bankCode = t.getSourceBank().getCode();
        BigDecimal share = config.isPercentage() ? config.getCollectingBankShare().multiply(t.getTransactionFee()) : config.getCollectingBankShare();

        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,bankCode,r);
    }

    private void doAggregatorShare(BaseTransaction t,EbillsBillingConfiguration config, Map<String,List<TransactionReport>> rptMap) {
        if(Utility.isEmptyBigDecimal(config.getAggregatorShare()))
            return;
        String aggCode = getAggregatorCodeForEbillsPay( t.getBiller().getId(), organizationRepository);
        if( aggCode == null)
            return;
        BigDecimal share = config.isPercentage() ? config.getAggregatorShare().multiply(t.getTransactionFee()) : config.getAggregatorShare();
        TransactionReport x = new TransactionReport(t);
        x.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap, aggCode, x);
    }

    private void doBillerBankShare(BaseTransaction t, EbillsBillingConfiguration config, Map<String,List<TransactionReport>> rptMap) {
        if( Utility.isEmptyBigDecimal(config.getBillerBankShare()) || config.getBillerBankCode() == null)
            return;
        BigDecimal share = config.isPercentage() ? config.getBillerBankShare().multiply(t.getTransactionFee()) : config.getBillerBankShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));

        sumCommission(rptMap,config.getBillerBankCode(),r);

    }

    private void doNibssShare(BaseTransaction t,EbillsBillingConfiguration config, Map<String,List<TransactionReport>> rptMap) {
        if(Utility.isEmptyBigDecimal(config.getNibssShare()))
            return;
        BigDecimal share = config.isPercentage() ? config.getNibssShare().multiply(t.getTransactionFee()) : config.getNibssShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,NIBSS_CODE,r);
    }
}

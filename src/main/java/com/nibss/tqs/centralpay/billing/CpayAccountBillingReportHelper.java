package com.nibss.tqs.centralpay.billing;

import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
import com.nibss.tqs.centralpay.dto.CpayAccountSharingConfig;
import com.nibss.tqs.centralpay.dto.CpayMerchant;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.report.BaseReportHelper;
import com.nibss.tqs.report.BillingReportGenerator;
import com.nibss.tqs.report.TransactionReport;
import com.nibss.tqs.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Emor on 9/27/16.
 */
@Component("cpayBillingReportHelper")
@Scope("prototype")
public class CpayAccountBillingReportHelper extends BaseReportHelper {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BillingReportGenerator billingReportGenerator;

    public void generateReports(List<AccountTransaction> transactions) throws Exception {
        Map<String,List<TransactionReport>> reportMap = new HashMap<>();


        Map<CpayMerchant,List<AccountTransaction>> byMerchant = transactions.stream().collect(Collectors.groupingBy(t -> t.getMerchant()));

        byMerchant.forEach((k,v) -> {

            CpayAccountSharingConfig config = k.getSharingConfig();
            v.forEach( t -> {
                doAggregatorShare(t,config,reportMap);
                doBillerBankShare(t,config,reportMap);
                doCollectingBankShare(t,config,reportMap);
                doNibssShare(t,config,reportMap);
            });
        });


        billingReportGenerator.generateReports(reportMap, Product.CENTRALPAY);
    }


    private void doCollectingBankShare(AccountTransaction t, CpayAccountSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(Utility.isEmptyBigDecimal(config.getCollectingBankShare()))
            return;
        String bankCode = t.getSourceBank().getCbnCode();
        BigDecimal share = config.isPercentage() ? config.getCollectingBankShare().multiply(t.getFee()) : config.getCollectingBankShare();

        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,bankCode,r);
    }

    private void doAggregatorShare(AccountTransaction t,CpayAccountSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(Utility.isEmptyBigDecimal(config.getAggregatorShare()))
            return;
        String aggCode = getAggregatorCodeForCentralPay( t.getMerchant().getMerchantCode(), organizationRepository);
        if( aggCode == null)
            return;
        BigDecimal share = config.isPercentage() ? config.getAggregatorShare().multiply(t.getFee()) : config.getAggregatorShare();
        TransactionReport x = new TransactionReport(t);
        x.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap, aggCode, x);
    }

    private void doBillerBankShare(AccountTransaction t, CpayAccountSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if( Utility.isEmptyBigDecimal(config.getBillerBankShare()) || config.getBillerBankCode() == null)
            return;
        BigDecimal share = config.isPercentage() ? config.getBillerBankShare().multiply(t.getFee()) : config.getBillerBankShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));

        sumCommission(rptMap,config.getBillerBankCode(),r);

    }

    private void doNibssShare(AccountTransaction t,CpayAccountSharingConfig config, Map<String,List<TransactionReport>> rptMap) {
        if(Utility.isEmptyBigDecimal(config.getNibssShare()))
            return;
        BigDecimal share = config.isPercentage() ? config.getNibssShare().multiply(t.getFee()) : config.getNibssShare();
        TransactionReport r = new TransactionReport(t);
        r.setCalculatedAmount(share.setScale(2, BillingProvider.ROUNDING_MODE));
        sumCommission(rptMap,NIBSS_CODE,r);
    }
}

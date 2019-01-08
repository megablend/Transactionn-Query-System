package com.nibss.tqs.ussd.merchantpay.billing;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.report.BaseReportHelper;
import com.nibss.tqs.report.BillingReportGenerator;
import com.nibss.tqs.report.TransactionReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 9/7/2016.
 */
@Component
@Scope("prototype")
@Slf4j
public class MerchantPaymentReportHelper extends BaseReportHelper {


    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private BillingReportGenerator billingReportGenerator;


    public void generateReports(List<DebitTransaction> transactions, MerchantPaymentSharingConfig config) throws Exception {

        if( config == null || transactions == null || transactions.isEmpty())
            return;

        Map<String,List<TransactionReport>> reportMap = new HashMap<>();

        List<DebitTransaction> trxn = new ArrayList<>(transactions);

        //group by source bank
        Map<String,List<TransactionReport>> bySourceBank = trxn.stream().map(t -> new TransactionReport(t))
                .collect(Collectors.groupingBy( t -> t.getSourceBankCode()));


        //do computation
        if( !isEmptyBigDecimal(config.getPayerBankShare())) {
            bySourceBank.forEach((k,v) -> {
                v.stream().forEach( t -> {
                    BigDecimal comm = config.getPayerBankShare().multiply(t.getFee());
                    t.setCalculatedAmount(comm.setScale(2, BillingProvider.ROUNDING_MODE));
                    sumCommission(reportMap,k,t);
                });
            });
        }


        //group by aggregator


        //do computation

        //group by ussd aggregator
        //do computation
        if( !isEmptyBigDecimal(config.getUssdAggregatorShare())) {

            Map<String,List<TransactionReport>> byUssdAggr = trxn.stream().map( t -> new TransactionReport(t))
                    .collect(Collectors.groupingBy(t -> t.getUssdAggregatorCode()));

            byUssdAggr.forEach( (k,v) -> v.stream().forEach(t -> {
                BigDecimal com = t.getFee().multiply(config.getUssdAggregatorShare());
                t.setCalculatedAmount(com.setScale(2, BillingProvider.ROUNDING_MODE));
                sumCommission(reportMap,k,t);
            }));
        }


        //group by telco
        if( !isEmptyBigDecimal(config.getTelcoShare())) {
            Map<String,List<TransactionReport>> byTelco = trxn.stream().map( t -> new TransactionReport(t))
                    .collect(Collectors.groupingBy(t -> t.getTelcoCode()));

            byTelco.forEach( (k,v) -> v.stream().forEach(t -> {
                BigDecimal com = t.getFee().multiply(config.getTelcoShare());
                t.setCalculatedAmount(com.setScale(2, BillingProvider.ROUNDING_MODE));
                sumCommission(reportMap,k,t);
            }));
            //do computation

        }

        //royalty scheme
        if(!isEmptyBigDecimal(config.getSchemeRoyaltyShare())) {
            List<TransactionReport> rptTrxns = trxn.stream().map( t -> new TransactionReport(t)).collect(Collectors.toList());
            rptTrxns.forEach( t -> {
                BigDecimal com = t.getFee().multiply(config.getSchemeRoyaltyShare());
                t.setCalculatedAmount(com.setScale(2, BillingProvider.ROUNDING_MODE));
                sumCommission(reportMap,config.getSchemeRoyaltyCode(),t);
            });
        }

        if(!isEmptyBigDecimal(config.getMerchantBankShare())) {
            Map<String,List<TransactionReport>> byDestBank = trxn.stream().map( t -> new TransactionReport(t))
                    .collect(Collectors.groupingBy(TransactionReport::getDestinationBankCode));

            byDestBank.forEach( (k,v) -> v.forEach(t -> {
                BigDecimal com = t.getFee().multiply(config.getMerchantBankShare());
                t.setCalculatedAmount(com.setScale(2, BillingProvider.ROUNDING_MODE));
                sumCommission(reportMap,k,t);
            }));
        }

        if(!isEmptyBigDecimal(config.getMerchantIntroducerShare())) {
            Map<Number,List<TransactionReport>> byMerchant = trxn.stream().map( t -> new TransactionReport(t))
                    .collect(Collectors.groupingBy(TransactionReport::getMerchantId));

            byMerchant.forEach( (k,v) -> {
                String aggCode = getAggregatorCodeForMerchantPay(k.longValue(), organizationRepository);
                if( null != aggCode) {
                    v.stream().forEach( t -> {
                        BigDecimal com = t.getFee().multiply(config.getMerchantIntroducerShare());
                        t.setCalculatedAmount(com.setScale(2, BillingProvider.ROUNDING_MODE));
                        sumCommission(reportMap,aggCode,t);
                    });
                }
            });
        }

        if(!isEmptyBigDecimal(config.getNibssShare())) {
            trxn.stream().forEach( t -> {
                BigDecimal com = t.getFee().multiply(config.getNibssShare());
                TransactionReport x = new TransactionReport(t);
                x.setCalculatedAmount(com.setScale(2, BillingProvider.ROUNDING_MODE));
                sumCommission(reportMap,NIBSS_CODE,x);
            });
        }
        //generate reports
        log.trace("about generating reports for {}", Product.USSD_MERCHANT_PAYMENT);
        billingReportGenerator.generateReports(reportMap, Product.USSD_MERCHANT_PAYMENT);
        log.trace("done generating reports for {}", Product.USSD_MERCHANT_PAYMENT);
    }


}

package com.nibss.tqs.ebillspay.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Created by eoriarewo on 7/26/2016.
 */
@Data
public class SharedTransaction implements Serializable{

    private BigDecimal nibssShare;
    private BigDecimal aggregatorShare;
    private BigDecimal billerBankShare;
    private BigDecimal collectingBankShare;
    private  EbillspayTransaction transaction;
    private EbillsBillingConfiguration billingConfiguration;


    public SharedTransaction(final EbillsBillingConfiguration config, final EbillspayTransaction transaction) {
        BigDecimal transactionFee = transaction.getBaseTransaction().getTransactionFee();
        if( config.isPercentage() ) {
            if( config.getNibssShare() != null)
                nibssShare = transactionFee.multiply(config.getNibssShare());
            if( config.getAggregatorShare() != null)
                aggregatorShare = transactionFee.multiply(config.getAggregatorShare());
            if( config.getBillerBankShare() != null)
                billerBankShare = transactionFee.multiply(config.getBillerBankShare());
            if( config.getCollectingBankShare() != null)
                collectingBankShare = transactionFee.multiply(config.getCollectingBankShare());
        } else {
            nibssShare = config.getNibssShare();
            aggregatorShare = config.getAggregatorShare();
            billerBankShare = config.getBillerBankShare();
            collectingBankShare = config.getCollectingBankShare();
        }

        this.transaction = transaction;
        this.transaction.setBilled(true);
        this.billingConfiguration = config;
       Arrays.asList(nibssShare,aggregatorShare,billerBankShare,collectingBankShare)
               .stream().filter(t -> t != null).forEach( t -> t = t.setScale(2, RoundingMode.HALF_DOWN));



    }
}

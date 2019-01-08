package com.nibss.tqs.ajax;

import com.nibss.tqs.ebillspay.dto.Biller;
import com.nibss.tqs.ebillspay.dto.EbillsBillingConfiguration;
import com.nibss.tqs.ebillspay.repositories.EbillsBillerService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

@Data
@NoArgsConstructor
public class AjaxEbillsPayBillers implements Serializable {

    private static final NumberFormat format = new DecimalFormat("#,##0.00##");
    private int id;
    private String billerName;
    private String aggregatorShare;
    private String collectingBankShare;
    private String nibssShare;
    private String billerBankShare;
    private String billerBankCode;
    private boolean percentage;


    public AjaxEbillsPayBillers(final Biller biller) {

        this.billerName = biller.getName();
        this.id = biller.getId();
        if( null != biller.getEbillsBillingConfigurations()) {
            EbillsBillingConfiguration config = biller.getEbillsBillingConfigurations();
            if( null != config.getAggregatorShare())
                aggregatorShare = format.format(config.getAggregatorShare());
            if( null != config.getCollectingBankShare())
                collectingBankShare = format.format(config.getCollectingBankShare());
            if( null != config.getNibssShare())
                nibssShare = format.format(config.getNibssShare());

            if( null != config.getBillerBankShare())
                billerBankShare = format.format(config.getBillerBankShare());

            percentage = config.isPercentage();
            billerBankCode = config.getBillerBankCode();

        }
    }


}

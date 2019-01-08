package com.nibss.tqs.core.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by eoriarewo on 8/15/2016.
 *
 * holds sharing info for merchant payment sharing.
 * There should be only one instance of this object
 */
@Entity
@Table(name="merchant_payment_sharing_config")
@Data
public class MerchantPaymentSharingConfig implements Serializable {

    private final  static  int SCALE = 5;
    private final static int PRECISION = 9;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="telco_share",precision = PRECISION, scale = SCALE)
    private BigDecimal telcoShare;

    @Column(name="payer_bank_share",precision = PRECISION, scale = SCALE)
    private BigDecimal payerBankShare;

    @Column(name="merchant_bank_share",precision = PRECISION, scale = SCALE)
    private BigDecimal merchantBankShare;

    @Column(name="ussd_aggregator_share",precision = PRECISION, scale = SCALE)
    private BigDecimal ussdAggregatorShare;

    //constant. Appears in all transactions. Hence, can be put in config location
    @Column(name="nibss_share",precision = PRECISION, scale = SCALE)
    private BigDecimal nibssShare;

    @Column(name="merchant_introducer_share",precision = PRECISION, scale = SCALE)
    private BigDecimal merchantIntroducerShare;

    //constant. NIBSS agreed to pay MTN for a while cos they brought the idea to NIBSS
    @Column(name = "scheme_loyalty_share",precision = PRECISION, scale = SCALE)
    private BigDecimal schemeRoyaltyShare;

    //the telco code of the scheme royalty beneficiary. MTN code in this case
    @Column(name = "scheme_royalty_code")
    private String schemeRoyaltyCode;

    @Column(name="created_by",nullable = false)
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "date_created",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();

    @Column(name = "date_modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModified;
}

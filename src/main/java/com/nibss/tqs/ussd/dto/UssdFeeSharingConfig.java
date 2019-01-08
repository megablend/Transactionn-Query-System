package com.nibss.tqs.ussd.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by eoriarewo on 8/15/2016.
 */
@Entity
@Table(name="ussd_fee_sharing_config")
@Data
public class UssdFeeSharingConfig implements Serializable {

    public static final int PRECISION = 9;
    public static final int SCALE = 5;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="aggregator_share", nullable = false, precision = PRECISION,scale = SCALE)
    private BigDecimal aggregatorShare;

    @Column(name="collecting_bank_share", nullable = false, precision = PRECISION,scale = SCALE)
    private BigDecimal collectingBankShare;

    @Column(name="biller_bank_share", nullable = false, precision = PRECISION,scale = SCALE)
    private BigDecimal billerBankShare;

    @Column(name="nibss_share", nullable = false, precision = PRECISION,scale = SCALE)
    private  BigDecimal nibssShare;

    @Column(name = "biller_bank_code",length = 10)
    private String billerBankCode;

    @Column(name="ussd_aggregator_share", nullable = false, precision = PRECISION,scale = SCALE)
    private BigDecimal ussdAggregatorShare;

    @Column(name="telco_share", nullable = false, precision = PRECISION,scale = SCALE)
    private BigDecimal telcoShare;

    @Column(name = "is_percentage")
    private  boolean percentage;

    @OneToOne
    @JoinColumn(name="merchant_id",referencedColumnName = "record_id")
    private UssdBiller ussdBiller;
}

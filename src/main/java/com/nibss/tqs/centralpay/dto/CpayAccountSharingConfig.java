package com.nibss.tqs.centralpay.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by eoriarewo on 9/6/2016.
 */
@Entity
@Table(name ="cpay_acct_fee_sharing_config")
@Data
public class CpayAccountSharingConfig implements Serializable {

    public static final int PRECISION = 10;
    public static final int SCALE = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "nibss_share", precision = PRECISION,scale = SCALE)
    private BigDecimal nibssShare;

    @Column(name = "collecting_bank_share", precision = PRECISION,scale = SCALE)
    private  BigDecimal collectingBankShare;

    @Column(name = "aggregator_share", precision = PRECISION,scale = SCALE)
    private BigDecimal aggregatorShare;

    @Column(name = "biller_bank_share",precision = PRECISION,scale = SCALE)
    private BigDecimal billerBankShare;

    @Column(name="biller_bank_code")
    private String billerBankCode;

    @Column(name = "is_percentage")
    private boolean percentage;

    @OneToOne
    @JoinColumn(name = "merchant_id",referencedColumnName = "record_id")
    private CpayMerchant merchant;
}

package com.nibss.tqs.centralpay.dto;

import com.nibss.tqs.ussd.dto.FeeType;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by eoriarewo on 9/14/2016.
 */
@Embeddable
@Access(AccessType.FIELD)
@Data
public class TransactionFee implements Serializable{

    @Column(name = "fee_type")
    @Enumerated(EnumType.ORDINAL)
    private FeeType feeType;

    @Column(name="fee_min")
    private BigDecimal amountFloor;

    @Column(name="fee_max")
    private BigDecimal amountCap;

    @Column(name="fee")
    private BigDecimal transactionFee;
}
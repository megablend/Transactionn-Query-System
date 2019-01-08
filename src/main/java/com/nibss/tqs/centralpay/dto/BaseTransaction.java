package com.nibss.tqs.centralpay.dto;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@MappedSuperclass
@Data
public abstract class BaseTransaction implements Serializable {

    protected static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    
    protected static final String SUCCESS = "00";

    @Id
    @Column(name = "cpay_txn_ref")
    @Getter
    protected String cpayRef;

    @Column(name = "merchant_txn_ref")
    protected String merchantRef;

    @Column(name = "payment_ref")
    protected String paymentRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "processor_id", referencedColumnName = "gateway_id")
    protected PaymentGateway paymentGateway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "merchant_id")
    protected CpayMerchant merchant;

    @Column(name = "amount", precision = 18, scale = 2)
    protected BigDecimal amount;

    @Column(name = "product_desc")
    protected String product;

    @PostLoad
    protected void checkProduct() {
        if (null != product) {
            product = product.replace("+", " ");
        }
    }
}

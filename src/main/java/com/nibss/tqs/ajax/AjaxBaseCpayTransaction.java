package com.nibss.tqs.ajax;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Created by eoriarewo on 7/4/2017.
 */
@Data
public class AjaxBaseCpayTransaction implements Serializable {

    protected static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    protected static final RoundingMode ROUNDING_MODE  = RoundingMode.HALF_EVEN;

    protected static final String SUCCESS = "00";

    public static final String UNKNOWN = "XXX";


    protected String cpayRef;

    protected String merchantRef;

    protected String paymentGateway;

    protected String merchantName;

    protected String merchantCode;

    protected BigDecimal amount;

    protected String productName;

    protected String responseCode;

    protected String responseDescription;

    protected String customerId;

    protected Date transactionDate;

    protected Date dateApproved;

    protected String paymentRef;

    protected String processorId;
}

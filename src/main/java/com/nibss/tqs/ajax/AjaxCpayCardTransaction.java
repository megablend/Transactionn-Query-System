package com.nibss.tqs.ajax;

import com.nibss.tqs.centralpay.dto.CentralPayProcessors;
import com.nibss.tqs.centralpay.dto.PaymentGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by eoriarewo on 7/4/2017.
 */
@Data
@NoArgsConstructor
public class AjaxCpayCardTransaction extends AjaxBaseCpayTransaction {

    public static final String PROCESSOR_ID = "00001";

    public AjaxCpayCardTransaction(
            String cpayRef,
            String merchantRef,
            String processorId,
            String merchantName,
            String merchantCode,
            BigDecimal amount,
            String productName,
            String responseCode,
            String responseDescription,
            String customerId,
            Date transactionDate,
            Date dateApproved
    ) {
        this.cpayRef = cpayRef;
        this.merchantRef = merchantRef;
        this.paymentGateway = CentralPayProcessors.getNameFromCode(processorId);

        this.processorId = processorId;
        this.merchantName = merchantName;
        this.merchantCode = merchantCode;
        this.amount = amount;
        this.productName = productName;
        this.responseCode = responseCode;
        this.responseDescription = responseDescription;
        this.customerId = customerId;
        this.transactionDate = transactionDate;
        this.dateApproved = dateApproved;

        initRecord();
    }

    private void initRecord() {

        if (null != amount)
            amount = amount.divide(ONE_HUNDRED).setScale(2, ROUNDING_MODE);
        else
            amount = BigDecimal.ZERO;

        if (null != productName) {
            productName = productName.trim().replace("+", " ");
            if (productName.length() > 50)
                productName = productName.substring(0, 50);

        }

        if (customerId != null) {
            if (customerId.length() > 50)
                customerId = customerId.substring(0, 50);
        }
        if (responseCode == null || responseCode.trim().isEmpty())
            responseCode = UNKNOWN;
    }
}

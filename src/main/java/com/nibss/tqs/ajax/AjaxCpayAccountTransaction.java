package com.nibss.tqs.ajax;

import com.nibss.tqs.centralpay.dto.FinancialInstitution;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by eoriarewo on 7/4/2017.
 */
@Data
@NoArgsConstructor
public class AjaxCpayAccountTransaction extends AjaxBaseCpayTransaction {

    private static final String SYSTEM_MALFUNCTION = "96";

    private static final String PROCESSOR_ID = "00003";

    private static final String IB = "cpay-ib";

    private String sourceSessionId;

    private String destinationSessionId;

    private String sourceBankName;

    private String paymentType;

    private BigDecimal fee;

    public AjaxCpayAccountTransaction(
            String merchantName,
            String merchantCode,
            String sourceSessionId,
            String cpayRef,
            String merchantRef,
            FinancialInstitution sourceBank,
            String paymentRef,
            String customerId,
            String product,
            BigDecimal netAmount,
            BigDecimal fee,
            Date transactionDate,
            String responseCode,
            String processorId
    ) {

        this.merchantName = merchantName;
        this.merchantCode = merchantCode;
        this.sourceSessionId = sourceSessionId;
        this.cpayRef = cpayRef;
        this.merchantRef = merchantRef;
        if (null != sourceBank)
            this.sourceBankName = sourceBank.getName();

        this.paymentRef = paymentRef;
        this.customerId = customerId;
        this.productName = product;
        this.amount = netAmount;
        this.fee = fee;
        this.transactionDate = transactionDate;
        this.responseCode = responseCode;
        this.processorId = processorId;

        initRecord();
    }


    private void initRecord() {
        if (paymentRef != null) {
            if (paymentRef.toLowerCase().startsWith(IB))
                paymentRef = "Internet Banking";
            else
                paymentType = "OTP";
        }

        /*if (null != amount)
            amount = amount.divide(ONE_HUNDRED).setScale(2, ROUNDING_MODE);*/
        if( null == amount)
            amount = BigDecimal.ZERO;

        if (null == fee)
            fee = BigDecimal.ZERO;

        if (productName != null) {
            productName = productName.trim().replace("+", " ");
            if (productName.length() > 30)
                productName = productName.substring(0, 30);

        }

        if (customerId != null) {
            customerId = customerId.replace("+", " ");
            if (customerId.length() > 30)
                customerId = customerId.substring(0, 30);
        }

        if (responseCode == null || responseCode.trim().isEmpty())
            responseCode = UNKNOWN;

        if(processorId == null)
            processorId = PROCESSOR_ID;
    }
}

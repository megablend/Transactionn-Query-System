package com.nibss.tqs.ajax;

import com.nibss.merchantpay.entity.CreditTransaction;
import com.nibss.tqs.report.NipResponseCodes;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by eoriarewo on 7/4/2017.
 */
@Data
public class AjaxMcashTransaction implements Serializable {

    private String merchantName;
    private String merchantCode;

    private String telcoName;

    private String ussdAggregator;

    private String sourceBankName;

    private String destinationBankName;

    private String sessionId;

    private String paymentReference;

    private String phoneNumber;

    private BigDecimal amount;

    private BigDecimal fee;

    private BigDecimal creditFee;

    private Date transactionDate;

    private String debitResponseCode;

    private String creditResponseCode;

    private String debitResponseDescription;

    private String creditResponseDescription;

    private String referenceCode;

    private CreditTransaction creditTransaction;


    public AjaxMcashTransaction(
            String merchantName,
            String merchantCode,
            String telcoName,
            String ussdAggregator,
            String sourceBankName,
//            String destinationBankName,
            String sessionId,
            String paymentReference,
            String referenceCode,
            String phoneNumber,
            BigDecimal amount,
            BigDecimal debitFee,
            /*BigDecimal creditFee,*/
            Date transactionDate,
            String debitResponseCode,
           /* String creditResponseCode*/
           CreditTransaction creditTransaction
    ) {
        this.merchantName = merchantName;
        this.merchantCode = merchantCode;
        this.telcoName = telcoName;
        this.ussdAggregator = ussdAggregator;
        this.sourceBankName = sourceBankName;
        this.destinationBankName = destinationBankName;
        this.sessionId = sessionId;
        this.paymentReference = paymentReference;
        this.referenceCode = referenceCode;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.fee = debitFee;
//        this.creditFee = creditFee;
        this.transactionDate = transactionDate;
        this.debitResponseCode = debitResponseCode;
//        this.creditResponseCode = creditResponseCode;
        if( null != creditTransaction) {
            this.creditResponseCode = creditTransaction.getResponseCode();
            this.creditFee = creditTransaction.getFee();
            destinationBankName = creditTransaction.getInstitution().getBankName();
        }


        initRecord();
    }

    private void  initRecord() {
        if( null != fee) {
            if(creditFee != null)
                fee = fee.add(creditFee);
        } else
            fee = BigDecimal.ZERO;

        if(null == amount)
            amount = BigDecimal.ZERO;

        if( debitResponseCode != null)
            debitResponseDescription = NipResponseCodes.getDescriptionForCode(debitResponseCode);

        if( creditResponseCode != null)
            creditResponseDescription = NipResponseCodes.getDescriptionForCode(creditResponseCode);

    }
}

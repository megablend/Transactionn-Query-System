package com.nibss.tqs.ajax;

import com.nibss.tqs.report.ChequeResponseCodes;
import com.nibss.tqs.report.NipResponseCodes;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by eoriarewo on 7/4/2017.
 */
@Data
@NoArgsConstructor
public class AjaxEbillsPayTransaction implements Serializable {

    private final static String AWAITING_CLEARING = "Awaiting Clearing";
    private final static String CHEQUE_CLEARED = "Cheque Cleared";

    public AjaxEbillsPayTransaction(
            String billerName,
            String sessionId,
            BigDecimal amount,
            BigDecimal fee,
            String sourceBankName,
            String destinationBankName,
            String productName,
            Date transactionDate,
            Date dateApproved,
            String responseCode,
            String customerNumber,
            boolean chequeTransaction,
            String chequeResponseCode,
            Date chequeConfirmationDate,
            boolean chequeConfirmed,
            String branchCode

    ) {
        this.billerName = billerName;
        this.sessionId = sessionId;
        this.amount = amount;
        this.fee = fee;
        this.sourceBankName = sourceBankName;
        this.destinationBankName = destinationBankName;
        this.productName = productName;
        this.transactionDate = transactionDate;
        this.dateApproved = dateApproved;
        this.responseCode = responseCode;
        this.customerNumber = customerNumber;
        this.chequeTransaction = chequeTransaction;
        this.chequeResponseCode = chequeResponseCode;
        this.chequeConfirmationDate = chequeConfirmationDate;
        this.chequeConfirmed = chequeConfirmed;
        this.branchCode = branchCode;

        initRecord();
    }

    private void initRecord() {
        if (chequeTransaction) {
                this.dateApproved = chequeConfirmationDate;
            if (this.chequeConfirmed) {
                this.responseCode = "00";
                this.responseDescription = CHEQUE_CLEARED;
            } else {
                this.responseCode = chequeResponseCode;
                if (null != responseCode) {
                    responseDescription = ChequeResponseCodes.getDescriptionForCode(responseCode);
                    responseDescription = responseDescription == null ? AWAITING_CLEARING : responseDescription;
                } else {
                    this.responseCode = "-1";
                    this.responseDescription = AWAITING_CLEARING;

                }
            }
        } else {
            if (responseCode == null)
                responseCode = "-1";

            responseDescription = NipResponseCodes.getDescriptionForCode(responseCode);
        }

        if (null == amount)
            amount = BigDecimal.ZERO;
        if (null == fee)
            fee = BigDecimal.ZERO;
    }


    //transaction details
    private String sessionId;


    //amount details
    private BigDecimal amount;
    private BigDecimal fee;


    //bank details
    private String sourceBankName;
    private String destinationBankName;


    //product details
    private String productName;

    //date details
    private Date transactionDate;
    private Date dateApproved;


    //response details
    private String responseCode;
    private String responseDescription;


    //other details
    private String customerNumber;
    private String billerName;


    //cheque details
    private boolean chequeTransaction;
    private String chequeResponseCode;
    private Date chequeConfirmationDate;
    private boolean chequeConfirmed;

    private String branchCode;

}

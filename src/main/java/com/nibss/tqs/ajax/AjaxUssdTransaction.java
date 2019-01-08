package com.nibss.tqs.ajax;

import com.nibss.tqs.report.NipResponseCodes;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by eoriarewo on 7/5/2017.
 */
@Data
public class AjaxUssdTransaction implements Serializable {

    private String merchantName;
    private String merchantCode;

    private String ussdAggregator;

    private String id;

    private String sessionId;
    private String destinationSessionId;

    private String transactionReference;

    private String phoneNumber;

    private String telcoName;

    private String sourceBankCode;

    private BigDecimal amount;

    private BigDecimal fee;

    private Date requestTime;

    private String debitResponseCode;

    private String creditResponseCode;

    private String debitResponseDescription;

    private String creditResponseDescription;


    public AjaxUssdTransaction(
            String id,
            String merchantName,
            String merchantCode,
            String ussdAggregator,
            String sessionId,
            String destinationSessionId,
            String phoneNumber,
            String telcoName,
            String sourceBankCode,
            BigDecimal amount,
            BigDecimal fee,
            Date requestTime,
            String debitResponseCode,
            String creditResponseCode
    ) {
        this.id = id;
        this.merchantName = merchantName;
        this.merchantCode = merchantCode;
        this.ussdAggregator = ussdAggregator;
        this.sessionId = sessionId;
        this.destinationSessionId = destinationSessionId;
        this.phoneNumber = phoneNumber;
        this.telcoName = telcoName;
        this.sourceBankCode = sourceBankCode;
        this.amount = amount;
        this.fee = fee;
        this.requestTime = requestTime;
        this.debitResponseCode = debitResponseCode;
        this.creditResponseCode = creditResponseCode;

        initRecord();
    }

    private void initRecord() {
        if (debitResponseCode != null)
            debitResponseDescription = NipResponseCodes.getDescriptionForCode(debitResponseCode);
        else
            debitResponseCode = "-1";


        if (creditResponseCode != null)
            creditResponseDescription = NipResponseCodes.getDescriptionForCode(creditResponseCode);
        else
            creditResponseCode = "-1";

        if( null == amount)
            amount = BigDecimal.ZERO;
        if( null == fee)
            fee = BigDecimal.ZERO;
    }


}

package com.nibss.tqs.ussd.dto;

import com.nibss.tqs.report.NipResponseCodes;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by eoriarewo on 8/15/2016.
 */
@Entity
@Table(name="ussd_acct_transaction")

@Data
public class UssdTransaction implements Serializable {

    @Id
    @Column(name = "USSD_TXN_REF")
    private String id;

    @Column(name = "amount", scale = 9, precision = 2)
    private BigDecimal amount;

    @Column(name = "fee_amount")
    private BigDecimal transactionFee;

    @ManyToOne
    @JoinColumn(name = "merchant_code",referencedColumnName = "merchant_code")
    private UssdBiller ussdBiller;

    @Column(name="src_response_code")
    private String sourceResponseCode;

    @Column(name="dest_response_code")
    private String destinationResponseCode;

    @Column(name = "src_session_id")
    private String sourceSessionId;

    @Column(name="dest_session_id")
    private  String destinationSessionId;

    @Column(name="net_amount")
    private BigDecimal netAmount;

    @Column(name="request_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    @Column(name="response_time")
    private Date responseDate;

    @Column(name="src_bank_code")
    private String sourceBankCode;

    @Column(name = "dest_bank_code")
    private String destinationBankCode;


    @Column(name = "is_billed")
    private boolean billed;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "telco", referencedColumnName = "telco_id")
    private UssdTelco telco;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requestor_id", referencedColumnName = "requestor_id")
    private UssdAggregator ussdAggregator;

    @Column(name="CUST_PHONE_NUMBER")
    private String phoneNumber;

    @Transient
    private String sourceResponseDescription;

    @Transient
    private String destinationResponseDescription;



    @PostLoad
    protected void doResponses() {
        if( null != sourceResponseCode && !sourceResponseCode.trim().isEmpty()) {
            sourceResponseDescription = getResponseDesc(sourceResponseCode);

        } else {
            sourceResponseCode = NipResponseCodes.UNAPPROVED.getResponseCode();
            sourceResponseDescription = NipResponseCodes.UNAPPROVED.getResponseDesc();
        }

        if( null != destinationResponseCode && !destinationResponseCode.trim().isEmpty()) {
            destinationResponseDescription = getResponseDesc(destinationResponseCode);
        }else {
            destinationResponseCode = NipResponseCodes.UNAPPROVED.getResponseCode();
            destinationResponseDescription = NipResponseCodes.UNAPPROVED.getResponseDesc();
        }
    }

    private String getResponseDesc(String code) {
        NipResponseCodes one = Arrays.asList(NipResponseCodes.values())
                    .stream().filter( t -> t.getResponseCode().equals(code)).findFirst().orElse(NipResponseCodes.UNAPPROVED);

        return  one.getResponseDesc();

    }
}

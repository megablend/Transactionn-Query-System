package com.nibss.tqs.centralpay.dto;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Entity
@Table(name = "cpay_acct_transaction")
@Data
public class AccountTransaction extends BaseTransaction{

    private static final String SUCCESS = "00";
    private static final String SYSTEM_MALFUNCTION ="96";

    private static final String IB = "cpay-ib";

    @Column(name="session_id", nullable = false)
    private String sourceSessionId;

    @Column(name="dest_session_id")
    private String destinationSessionId;

    @ManyToOne
    @JoinColumn(name = "bank_code",referencedColumnName = "old_code",foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private FinancialInstitution sourceBank;

    @Column(name = "dest_bank_code")
    private String destinationBankCode;

    @Column(name="request_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @Column(name="response_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateApproved;


    @Column(name="customer_id")
    private String customerId;

    @Column(name="fee_amount")
    private BigDecimal fee;

    @Column(name="net_amount")
    private BigDecimal netAmount;

    @Column(name="billed")
    private boolean billed;

    @Column(name="processor_id",insertable = false, updatable = false)
    private String processorId;

    @Column(name="response_code")
    private String responseCode;

    @Transient
    private String paymentType;

    @PostLoad
    private void setFields() {

        if(paymentRef != null) {
            if(paymentRef.toLowerCase().startsWith(IB) )
                paymentType = "Internet Banking";
            else
                paymentType = "OTP";

        }
        
         //amount is presently stored in kobo in DB
        if( amount != null)
            amount = amount.divide(ONE_HUNDRED).setScale(2, RoundingMode.HALF_EVEN);

    }
}

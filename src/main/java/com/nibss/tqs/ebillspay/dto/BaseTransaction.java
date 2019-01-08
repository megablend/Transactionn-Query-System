package com.nibss.tqs.ebillspay.dto;

import com.nibss.tqs.report.NipResponseCodes;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by eoriarewo on 7/8/2016.
 */
@Entity
@Table(name = "transactions")
@Data
public class BaseTransaction implements Serializable {

    private static final String CHEQUE_CLEARED = "Cheque Cleared";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private long id;

    @Column(name = "transaction_id")
    @Getter
    @Setter
    private String sessionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_bank_code", referencedColumnName = "sort_code", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Getter
    @Setter
    private Bank sourceBank;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_bank_code", referencedColumnName = "sort_code", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Getter
    @Setter
    private Bank destinationBank;

    @OneToMany(mappedBy = "baseTransaction", fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Set<EbillspayTransaction> ebillspayTransaction = new HashSet();

    @Column(name = "amount", nullable = true, precision = 18, scale = 2)
    private BigDecimal amount;


    @Column(name = "transaction_fee", nullable = true, precision = 18, scale = 2)
    private BigDecimal transactionFee;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @Column(name = "nip_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateApproved;

    @Column(name = "channel_code")
    private int channelCode;


    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    @OrderBy("name ASC")
    private List<UserParam> userParams;

    @Column(name = "nip_response_code")
    private String responseCode;

    @Transient
    private String responseDescription;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "biller_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Biller biller;

    @Column(name = "customer_no")
    private String customerNumber;

    @Column(name = "cheque_payment", nullable = false)
    private boolean cheque;

    @Column(name = "cheque_confirmation_status", nullable = false)
    private boolean chequeConfirmationStatus;


    @Column(name = "cheque_confirmation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date chequeConfirmationDate;

    @Column(name = "cheque_response")
    private String chequeResponse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;


    @Column(name="branch_code")
    private String branchCode;

    @PostLoad
    protected void changeStatus() {

        if (cheque) {
            if (chequeConfirmationStatus) {
                dateApproved = chequeConfirmationDate;
                responseCode = "00";
                responseDescription = CHEQUE_CLEARED;
            } else {
                responseCode = "-1";
                responseDescription = "Awaiting Clearing";
            }
        } else if( responseCode == null || responseCode.equals("-1")) {
            responseDescription = "Unapproved";
            responseCode = "-1";
        } else
            responseDescription = getResponseDesc(responseCode);


        if (amount == null)
            amount = BigDecimal.ZERO;
        if (null == transactionFee)
            transactionFee = BigDecimal.ZERO;

    }

    private String getResponseDesc(String code) {
        NipResponseCodes one = Arrays.asList(NipResponseCodes.values())
                .stream().filter(t -> t.getResponseCode().equals(code)).findFirst().orElse(NipResponseCodes.UNAPPROVED);

        return one.getResponseDesc();

    }
}

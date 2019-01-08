package com.nibss.tqs.ebillspay.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by Emor on 7/1/16.
 *
 *  +
 " AND t.biller.ebillsBillingConfigurations IS NOT NULL"
 */
@Entity
@Table(name="tqs_transaction_historys")
@NamedQueries({
    @NamedQuery(name="EbillspayTransaction.findUnsharedTransactions",
    query = "SELECT t FROM EbillspayTransaction t WHERE t.shared=false AND t.baseTransaction.transactionFee > 0 AND" +
            " (t.baseTransaction.responseCode='00' OR t.baseTransaction.chequeResponse = '00' )" +
            " AND t.baseTransaction.biller.id IN (SELECT e.biller.id FROM EbillsBillingConfiguration e)")
})
@Data
public class EbillspayTransaction implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1973835932874353473L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

 /*   @Column(name="session_id", length = 30, nullable = false, insertable = false, updatable = false)
    @Getter @Setter
    private String sessionId;*/



    @Column(name="is_billed",nullable = false)
    private boolean billed;


    @Column(name="nibss_share")
    private BigDecimal nibssShare;

    @Column(name="aggregator_share")
    private BigDecimal aggregatorShare;

    @Column(name="biller_bank_share")
    private BigDecimal billerBankShare;

    @Column(name="collecting_bank_share")
    private BigDecimal collectingBankShare;

    @Column(name="is_shared")
    private boolean shared;


    @Column(name="source_bank")
    private String sourceBank;


    @ManyToOne( fetch = FetchType.EAGER)
    @JoinColumn(name="transaction_id", referencedColumnName = "transaction_id")
    private BaseTransaction baseTransaction;


}

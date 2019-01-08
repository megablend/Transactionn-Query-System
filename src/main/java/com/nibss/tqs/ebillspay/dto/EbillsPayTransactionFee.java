package com.nibss.tqs.ebillspay.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="ebillspay_biller_transaction_fee")
public class EbillsPayTransactionFee implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9021609514045597540L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Getter @Setter
	private int id;
	
	/**
	 * the agreed transaction fee for the biller. If it is a percentage, this should be the fraction.
	 * <p>Example:<br/><br/>
	 * if percentage is 2%, then assigned value should be <b>0.02</b> instead of <b>2</b>.
	 * </p>
	 */
	@Column(name="transaction_fee",nullable=false, precision=18, scale=5)
	@Getter @Setter
	@NotNull(message = "Fee was not specified")
	private BigDecimal fee;
	
	/**
	 * the upper bound of the transaction fee. This usually applies when fee is a percentage.
	 */
	@Column(name="amount_cap", precision=18, scale=2)
	@Getter @Setter
	private BigDecimal amountCap;
	
	/**
	 * the lower bound of the transaction fee. This usually applies when fee is a percentage
	 */
	@Column(name="amount_floor", precision=18, scale=2)
	@Getter @Setter
	private BigDecimal amountFloor;
	
	/**
	 * indicates if the fee is captured as a percentage.
	 * <code>true</code> if fee is captured as percentage. <code>false</code> if not
	 */
	@Column(name="is_percentage")
	@Getter @Setter
	private boolean percentage;
	
	/**
	 * indicates if the customer bares the fee. 
	 * <code>true</code> if customer pays. <code>false</code> if biller pays
	 */
	@Column(name="customer_pays")
	@Getter @Setter
	private boolean customerPays;
	
	/**
	 * indicates if the fee is taken at transaction time or not
	 */
	@Column(name="transaction_time_taken")
	@Getter @Setter
	private boolean transactionTimeTaken;
	
	@Getter @Setter
	@OneToOne(optional=false)
	@JoinColumn(name="biller_id", referencedColumnName="id")
	private Biller biller;

	@Column(name = "created_by")
	@Getter @Setter
	private String createdBy;

	@Getter @Setter
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="date_created")
	private Date dateCreated = new Date();

	@Column(name="modified_by")
	@Getter @Setter
	private String modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="date_modified")
	@Getter @Setter
	private Date dateModified;
}

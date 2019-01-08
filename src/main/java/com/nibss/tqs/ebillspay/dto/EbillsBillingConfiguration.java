package com.nibss.tqs.ebillspay.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="billing_sharing_formula")
@NoArgsConstructor
public class EbillsBillingConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8988994822883008071L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Getter
	private int id;
	
	@Getter @Setter
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="biller_id", referencedColumnName="id")
	private Biller biller;
	
	@Column(name="is_percentage")
	@Getter @Setter
	private boolean percentage;
	
	@Column(name="nibss_share", precision=18, scale=6)
	@Getter @Setter
	private BigDecimal nibssShare;
	
	@Column(name="collecting_bank_share", precision=18, scale=6)
	@Getter @Setter
	private BigDecimal collectingBankShare;
	
	@Column(name="biller_bank_share",precision=18, scale=6)
	@Getter @Setter
	private BigDecimal billerBankShare;

	@Column(name="aggregator_share", precision = 18, scale = 6)
	@Getter @Setter
	private BigDecimal aggregatorShare;


	@Column(name="biller_bank_code")
	@Getter @Setter
	private String billerBankCode;
	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="date_added")
	@Getter
	private Date dateCreated;


	@Transient
	@Getter @Setter
	private String billerName;

	@PrePersist
	protected void prePersist() {
		dateCreated = new Date();
		Arrays.asList(nibssShare,aggregatorShare,collectingBankShare,billerBankShare)
				.stream().filter( s -> s == null).forEach( e -> e = BigDecimal.ZERO);
	}

	public EbillsBillingConfiguration(String billerName, final EbillsBillingConfiguration config) {
		this.billerName = billerName;
		this.percentage = config.percentage;
		this.nibssShare = config.nibssShare;
		this.aggregatorShare = config.aggregatorShare;
		this.billerBankCode = config.billerBankCode;
		this.billerBankShare = config.billerBankShare;
		this.collectingBankShare = config.collectingBankShare;
	}
}

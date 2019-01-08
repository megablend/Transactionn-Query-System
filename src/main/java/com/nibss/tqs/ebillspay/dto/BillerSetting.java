package com.nibss.tqs.ebillspay.dto;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="biller_settings")
public class BillerSetting  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7490031879253904508L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Getter @Setter
	private int id;
	
	@OneToOne(optional=false)
	@JoinColumn(name="biller_id", referencedColumnName="ID")
	@Setter @Getter
	private Biller biller;
	
	
	@Column(name="createdBy", length=200)
	@Getter @Setter
	private String createdBy;
	
	@Column(name="modifiedBy",length=200)
	private String modifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dateCreated")
	@Setter @Getter
	private Date dateCreated;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dateModified",nullable=true)
	@Getter @Setter
	private Date dateModified;
	
	/**
	 * the param name captured for the biller that can serve as the primary identifier for a transaction
	 */
	@Column(name="param_name",nullable=true)
	@Getter @Setter
	private String paramName;


	@Column(name="duration")
	private int duration = 0;

	/**
	 * the billing cycle for which billing will occur. Weekly or monthly
	 */

	@Column(name="billing_cycle")
	@Getter @Setter
	@Enumerated(EnumType.ORDINAL)
	@NotNull(message = "Please specify billing cycle")
	private BillingCycle billingCycle;


	@PrePersist
	protected  void prePersist() {
		this.dateCreated = new Date();
	}

}

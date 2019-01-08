package com.nibss.tqs.core.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aggregators")
@DiscriminatorValue(OrganizationType.AGGREGATOR)
@PrimaryKeyJoinColumn(name=OrganizationType.ORGANIZATION_ID)
public class Aggregator extends Organization {


	/**
	 * 
	 */
	private static final long serialVersionUID = 7224153833916103887L;

	public Aggregator(){

	}

	public Aggregator(final Organization organization) {
		this.setCreatedBy(organization.getCreatedBy());
		this.setName(organization.getName());
		this.setOrganizationSetting(organization.getOrganizationSetting());
		if( null != organization.getOrganizationSetting())
			organization.getOrganizationSetting().setOrganization(this);
		this.setCentralPayMerchantCodes(organization.getCentralPayMerchantCodes());
		this.setEbillspayBillerIds(organization.getEbillspayBillerIds());
		this.setMerchantPaymentIds(organization.merchantPaymentIds);
		this.setUssdBillerCodes( organization.ussdBillerCodes);

	}
	/**
	 * The aggregator code. In most cases, this is the same as the
	 * RC number of the company.
	 */
	@Getter @Setter
	@NotNull(message="Aggregator Code cannot be empty")
	@Column(unique = true, nullable = false)
	private String code;
}

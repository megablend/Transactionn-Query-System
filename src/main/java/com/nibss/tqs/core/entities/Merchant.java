package com.nibss.tqs.core.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name="merchant")
@DiscriminatorValue(OrganizationType.MERCHANT)
@PrimaryKeyJoinColumn(name=OrganizationType.ORGANIZATION_ID)
public class Merchant extends Organization{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3853141546891318434L;

	public Merchant(){

	}

	public Merchant(final Organization organization) {
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

	
}

package com.nibss.tqs.core.entities;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="banks")
@DiscriminatorValue(OrganizationType.BANK)
@PrimaryKeyJoinColumn(name=OrganizationType.ORGANIZATION_ID)
@NamedQueries(
		{
				@NamedQuery(name="Bank.findByCode", query = "SELECT b FROM Bank b WHERE b.cbnBankCode =?1 OR b.nipCode=?1")
		}
)
public class Bank extends Organization {

	public  Bank(){

	}

	public Bank(final Organization organization) {
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
	 * 
	 */
	private static final long serialVersionUID = -2007269790448983940L;

	@Getter @Setter
	@Column(name="cbn_code",nullable=false, unique=true)
	private String cbnBankCode;

	@Column(name="nip_code", nullable = false, unique = true, length = 15)
	@Getter @Setter
	private String nipCode;
}

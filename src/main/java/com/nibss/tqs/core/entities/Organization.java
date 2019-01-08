package com.nibss.tqs.core.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="organization_type",discriminatorType=DiscriminatorType.INTEGER)
@DiscriminatorValue(OrganizationType.NIBSS)
@ToString(of = {"name","id"})
@EqualsAndHashCode(of="id")
@NamedQueries({

		/* all product aggregators */
		@NamedQuery(name="Organization.findAllEbillsPayAggregators",
		query = "SELECT o FROM Organization o WHERE COUNT(o.ebillspayBillerIds) > 0 AND (o.organizationType = 2 OR o.organizationType = 3) "),

		@NamedQuery(name="Organization.findAllMerchantPaymentAggregators",
				query = "SELECT o FROM Organization o WHERE COUNT(o.merchantPaymentIds) > 0 AND (o.organizationType = 2 OR o.organizationType = 3) "),

		@NamedQuery(name="Organization.findAllCentralPayAggregators",
				query = "SELECT o FROM Organization o WHERE COUNT(o.centralPayMerchantCodes) > 0 AND (o.organizationType = 2 OR o.organizationType = 3) "),

		@NamedQuery(name="Organization.findAllUssdAggregators",
				query = "SELECT o FROM Organization o WHERE COUNT(o.ussdBillerCodes) > 0 AND (o.organizationType = 2 OR o.organizationType = 3) "),


		/* aggregator per product */
		@NamedQuery(
				name = "Organization.findAggregatorForEbillsPayBiller",
				query = "SELECT o FROM Organization o, IN (o.ebillspayBillerIds) y WHERE y=?1 AND (o.organizationType = 2 OR o.organizationType = 3) "
		),

		@NamedQuery(name = "Organization.findAggregatorForMerchantPaymentMerchant",
		query = "SELECT o FROM Organization o, IN (o.merchantPaymentIds) x WHERE x = ?1 AND (o.organizationType = 2 OR o.organizationType = 3) "),

		@NamedQuery(name = "Organization.findAggregatorForUssdBiller",
		query = "SELECT o FROM Organization o, IN (o.ussdBillerCodes) x WHERE x = ?1 AND (o.organizationType = 2 OR o.organizationType = 3)"),

		@NamedQuery(name="Organization.findAggregatorForCentralPayMerchant",
		query = "SELECT o FROM Organization o, IN (o.centralPayMerchantCodes) x WHERE x = ?1 AND (o.organizationType = 2 OR o.organizationType = 3)"),


		/* merchant queries */
		@NamedQuery(name="Organization.findEbillspayMerchant",
		query = "SELECT o FROM Organization o, IN(o.ebillspayBillerIds) x WHERE x = ?1 AND o.organizationType = 1"),

		@NamedQuery(name="Organization.findCentralPayMerchant",
				query = "SELECT o FROM Organization o, IN(o.centralPayMerchantCodes) x WHERE x = ?1 AND o.organizationType = 1"),

		@NamedQuery(name="Organization.findMerchantPayMerchant",
				query = "SELECT o FROM Organization o, IN(o.merchantPaymentIds) x WHERE x = ?1 AND o.organizationType = 1"),

		@NamedQuery(name="Organization.findUssdMerchant",
				query = "SELECT o FROM Organization o, IN(o.ussdBillerCodes) x WHERE x = ?1 AND o.organizationType = 1")


})
public class Organization extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1947540171373449546L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Getter
	private int id;
	
	@Getter @Setter
	@Column(unique = true, length = 300, nullable = false)
	@NotNull(message = "Please specify a Name")
	protected String name;
	
	@Column(name="organization_type",nullable = false, insertable = false, updatable = false)
	@Getter @Setter
	private int organizationType;
	

	@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "organizations")
	@OrderBy("name ASC")
	@Getter @Setter
	protected Set<Product> products = new HashSet<>(0);
	
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(name = "organization_ebills_billers",joinColumns =@JoinColumn(name="organization_id") )
	@Column(name="biller_id")
	@Getter @Setter
	protected Set<Integer> ebillspayBillerIds = new HashSet<>(0);
	
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(name="organization_cpay_merchants",joinColumns = @JoinColumn(name="organization_id"))
	@Column(name="merchant_id")
	@Getter @Setter
	protected Set<String> centralPayMerchantCodes = new HashSet<>(0);


	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "organization_merhantpay_merchants", joinColumns = @JoinColumn(name = "organization_id"))
	@Column(name="merchant_id")
	@Getter @Setter
	protected Set<Long> merchantPaymentIds = new HashSet<>(0);


	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name="organization_ussd_billers", joinColumns = @JoinColumn(name = "organization_id"))
	@Column(name="ussd_merchant_code")
	@Getter @Setter
	protected  Set<String> ussdBillerCodes = new HashSet<>(0);


	@OneToMany(mappedBy = "organization",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Getter @Setter
	private Set<User> users;

	@OneToOne(mappedBy = "organization", cascade =  CascadeType.ALL)
	@Getter @Setter
	private OrganizationSetting organizationSetting;


	@OneToMany(mappedBy = "organization", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	@Getter @Setter
	private Set<BankAccount> bankAccounts = new HashSet<>(0);


	@Column(name="created_by")
	@Getter @Setter
	private String createdBy;

}

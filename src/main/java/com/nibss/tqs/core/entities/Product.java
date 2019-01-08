package com.nibss.tqs.core.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="products")
@EqualsAndHashCode(of = "id")
public class Product extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5475685941159773250L;

	public static final String EBILLSPAY = "EBILLS";
	public static final String CENTRALPAY = "CPAY";
	public static final String USSD_MERCHANT_PAYMENT = "MPAY";
	public static final String USSD_BILL_PAYMENT = "BPAY";

	public static final String CORPORATE_LOUNGE = "CLOUNGE";
	public static final String CLOUNGE_PER_TRANSACTION = "CLOUNGE_PER_TRANSACTION";
	public static final String CLOUNGE_ANNUAL = "CLOUNGE_ANNUAL";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Getter
	private int id;
	
	@Column(nullable=false,length=300)
	@Getter @Setter
	private String name;

	@Column(nullable = false, unique = true, length = 50)
	@Getter @Setter
	private String code;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="organization_products")
	@Getter @Setter
	private Set<Organization> organizations = new HashSet<>(0);

	@OneToMany(mappedBy = "product",fetch = FetchType.LAZY)
	private Set<BankAccount> bankAccount = new HashSet<>(0);
}

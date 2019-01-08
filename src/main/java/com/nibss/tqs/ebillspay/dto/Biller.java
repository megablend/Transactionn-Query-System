package com.nibss.tqs.ebillspay.dto;

import java.io.Serializable;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table( name="billers")
@EqualsAndHashCode(of="id")
/*@NamedQueries(
		{
				@NamedQuery(name="Biller.findByNameLike", query ="SELECT b FROM Biller b WHERE b.name LIKE '?1%'")
		}
)*/
public class Biller implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2715364521373939790L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Getter
	private int id;
	
	@Column(name="name",nullable=false)
	@Getter @Setter
	private String name;
	
        @Column(name="activated")
        @Getter @Setter
	private boolean approved;
	
	
	@OneToOne(mappedBy="biller", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	@Getter @Setter
	private BillerSetting billerSetting;
	
	@OneToOne(mappedBy="biller", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
	@Getter @Setter
	private EbillsPayTransactionFee ebillsPayTransactionFee;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "biller")
	@Getter @Setter
	private List<BaseTransaction> transactions = new ArrayList<>(0);
	
	@OneToOne(mappedBy="biller", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@Getter @Setter
	private EbillsBillingConfiguration ebillsBillingConfigurations;


	
}

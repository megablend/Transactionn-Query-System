package com.nibss.tqs.centralpay.dto;

import com.nibss.tqs.ussd.dto.TransactionFeeConfig;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Entity
@Table(name="cpay_merchant_info")
@NamedQueries(
        {
                @NamedQuery(name = "CpayMerchant.findByMerchantCodes", query = "SELECT c FROM CpayMerchant c WHERE c.merchantCode IN ?1")
        }
)
public class CpayMerchant implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="record_id")
    @Getter
    private int id;

    @Column(name="merchant_name")
    @Getter @Setter
    private String name;

    @Column(name="merchant_id")
    @Getter @Setter
    private String merchantCode;

    @OneToMany(fetch = FetchType.LAZY)
    @Getter
    private List<CardTransaction> cardTransactions;

    @OneToMany(fetch = FetchType.LAZY)
    @Getter
    private List<AccountTransaction> accountTransactions;

    @OneToOne(mappedBy = "merchant",cascade = CascadeType.ALL)
    @Getter @Setter
    private CpayAccountSharingConfig sharingConfig;

    @Embedded
    @Getter
    private TransactionFee transactionFee;
}

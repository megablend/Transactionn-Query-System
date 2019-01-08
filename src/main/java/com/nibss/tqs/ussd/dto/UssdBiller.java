package com.nibss.tqs.ussd.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eoriarewo on 8/15/2016.
 */
@Entity
@Table(name="ussd_biller_information")
@Data
@NamedQueries(
        {
                @NamedQuery(name="UssdBiller.findByCodes", query = "SELECT u FROM UssdBiller u WHERE u.merchantCode IN ?1")
        }
)
@EqualsAndHashCode( of = {"id"})
public class UssdBiller implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private int id;

    @Column(name = "merchant_code")
    private String merchantCode;

    @Column(name="merchant_name")
    private String name;

    @Embedded
    private TransactionFeeConfig transactionFeeConfig;

    @OneToMany(mappedBy = "ussdBiller", fetch = FetchType.LAZY)
    private List<UssdTransaction> transactions = new ArrayList<>(0);

    @OneToOne(mappedBy = "ussdBiller",cascade = CascadeType.ALL)
    private UssdFeeSharingConfig feeSharingConfig;
}

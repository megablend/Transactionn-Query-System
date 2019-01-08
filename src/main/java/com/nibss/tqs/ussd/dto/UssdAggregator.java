package com.nibss.tqs.ussd.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by eoriarewo on 8/30/2016.
 */
@Entity
@Table(name="ussd_requestor_profile")
@Data
public class UssdAggregator implements Serializable {

    @Id
    @Column(name="requestor_id")
    private String id;

    @Column(name = "requestor_name")
    private String name;

    @Column(name = "acct_number")
    private String accountNumber;

    @Column(name = "acct_name")
    private String accountName;

    @Column(name = "bank_code")
    private String bankCode;

    @OneToMany(mappedBy = "ussdAggregator")
    private Set<UssdTransaction> transactions = new HashSet<>(0);
}

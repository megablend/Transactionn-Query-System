package com.nibss.tqs.centralpay.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Entity
@Table(name="cpay_gateway_info")
public class PaymentGateway implements Serializable {

    @Id
    @Column(name="gateway_id")
    @Getter
    private String gatewayId;

    @Column(name="gateway_name",nullable = false)
    @Getter @Setter
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "paymentGateway")
    private List<CardTransaction> cardTransactions;
}

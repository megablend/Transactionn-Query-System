package com.nibss.tqs.ebillspay.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by eoriarewo on 7/8/2016.
 */
@Entity(name="banks")
@Table(name="banks")
public class Bank implements Serializable {

    @Id
    @Getter
    private int id;

    @Column(name="name")
    @Getter @Setter
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "destinationBank")
    @Getter
    private List<BaseTransaction> destinationTransactions;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sourceBank")
    @Getter
    private List<BaseTransaction> sourceTransactions;


    @Column(name="sort_code")
    @Getter
    private String code;



}

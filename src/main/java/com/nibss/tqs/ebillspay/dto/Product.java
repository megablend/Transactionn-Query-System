package com.nibss.tqs.ebillspay.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emor on 3/3/17.
 */
@Entity
@Table(name = "products")
@Data
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;



    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    private List<BaseTransaction> transaction = new ArrayList<>();
}

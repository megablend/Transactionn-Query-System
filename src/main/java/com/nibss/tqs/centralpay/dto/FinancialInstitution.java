package com.nibss.tqs.centralpay.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by eoriarewo on 9/5/2016.
 */
@Entity
@Table(name="cpay_nip_version_code")
@Data
public class FinancialInstitution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="new_code")
    private String nipCode;

    @Column(name="old_code")
    private String cbnCode;

    @Column(name="institution_name")
    private String name;

    @OneToMany(mappedBy = "sourceBank")
    private Set<AccountTransaction> transactions = new HashSet<>(0);
}

package com.nibss.tqs.core.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by eoriarewo on 7/18/2016.
 */
@Entity
@Table(name = "organization_settings")
public class OrganizationSetting  implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private int id;

    @Column(name="no_operators", nullable = false)
    @Min(value = 1, message = "Number of operators cannot be less than 1")
    @Max(value = 20, message = "Number of operators cannot be greater than 20")
    @Getter @Setter
    private int noOfOperators;

    @Column(name="no_admins",nullable=false)
    @Min(value = 1, message = "Number of Admins cannot be less than 1")
    @Max(value = 5, message = "Number of Admins cannot be greater than 5")
    @Getter @Setter
    private int noOfAdmins;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @Getter @Setter
    private Organization organization;

    /**
     * specifies if the organization is allowed to view
     * ebillspay by transaction date (date initiated) as well.
     * By default, organizations only see transactions by date approved
     */
    @Getter @Setter
    @Column(name="view_ebillspay_by_transaction_date")
    private boolean ebillspayTransactionDateAllowed;

    @Getter @Setter
    @Column(name="modified_by")
    private String modifiedBy;

    @Getter @Setter
    @Column(name="date_modified")
    private Date dateModified;
}

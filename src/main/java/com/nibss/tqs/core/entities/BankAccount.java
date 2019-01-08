package com.nibss.tqs.core.entities;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by eoriarewo on 8/18/2016.
 */
@Entity
@Table(name="bank_accounts")
@Data
@NamedQueries(
        {
                @NamedQuery(name="BankAccount.findByOrganizationAndProductCode",
                        query = "SELECT b FROM BankAccount b WHERE b.organization.id=?1 AND b.product.code=?2"),
                @NamedQuery(name="BankAccount.findByOrganization",
                query = "SELECT new com.nibss.tqs.core.entities.BankAccount(b.bankCode,b.accountName,b.accountNumber,b.product.name) FROM BankAccount b WHERE b.organization.id = ?1")
        }
)
@EqualsAndHashCode(of = {"bankCode","accountNumber"})
public class BankAccount implements Serializable {

    public BankAccount(String accountName, String accountNumber, String bankCode) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="bank_code", length = 10, nullable = false)
    @NotBlank(message = "Bank code was not specified")
    private String bankCode;

    @Column(name = "account_number", length = 20, nullable = false)
    @NotBlank(message = "Account Number was not specified")
    private  String accountNumber;

    @Column(name = "account_name", nullable = false)
    @NotBlank(message = "Account Name was not specified")
    private String accountName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id",referencedColumnName = "id")
    private Product product;


    @ManyToOne
    @JoinColumn(name = "organization_id",referencedColumnName = "id")
    private Organization organization;

    @Transient
    private String productName;

    public BankAccount() {

    }

    public BankAccount(String bankCode, String accountName, String accountNumber, String productName) {
        this.bankCode = bankCode;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.productName = productName;
    }
}

package com.nibss.tqs.centralpay.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Entity
@Table(name="cpay_card_transaction")
@Data
@EqualsAndHashCode(of = {"cpayRef"},callSuper = false)
public class CardTransaction extends BaseTransaction{

   


    @Column(name="trans_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @Column(name = "response_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateApproved;

    @Column(name="response_code")
    private String responseCode;

    @Column(name="response_desc")
    private String responseDescription;


    @Column(name="processor_id", updatable = false, insertable = false)
    private String processorId;

    @Column(name="product_id")
    private String customerId;


    @PostLoad
    protected void postLoad() {
        //amount is presently stored in kobo in DB
        if( amount != null)
            amount = amount.divide(ONE_HUNDRED).setScale(2, RoundingMode.HALF_EVEN);
        
//        if( responseCode != null) {
//            if( responseCode.equals(SUCCESS))
//                responseDescription = "Approved or Completed Successfully";
//        }
    }
}

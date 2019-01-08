package com.nibss.tqs.renderer;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by eoriarewo on 9/1/2016.
 */
@Data
public class TransactionSummary implements Serializable {

    private String label;
    private long volume;
    private String totalAmount;
    private String transactionFee;
}

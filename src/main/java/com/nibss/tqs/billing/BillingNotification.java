package com.nibss.tqs.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by eoriarewo on 8/29/2016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingNotification implements Serializable {

    private String billingFilePath;
    private String product;
    private String period;
}

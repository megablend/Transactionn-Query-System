package com.nibss.tqs.corporatelounge.ajax;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by eoriarewo on 10/4/2017.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceDto implements Serializable {

    private String accountNumber;
    private String accountName;
    private String organization;
    private String bankName;
    private String email;
    private Date dateAdded;


    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String ACCOUNT_NAME = "accountName";

    public static final String ORGANIZATION = "organization";

    public static final String BANK = "bankName";

    public static final String DATE = "dateAdded";
}

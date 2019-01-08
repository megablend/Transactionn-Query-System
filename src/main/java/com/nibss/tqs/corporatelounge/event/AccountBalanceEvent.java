package com.nibss.tqs.corporatelounge.event;

import com.nibss.corporatelounge.dto.AccountBalance;
import lombok.Getter;

import java.io.Serializable;

/**
 * Created by eoriarewo on 10/5/2017.
 */

public class AccountBalanceEvent  implements Serializable{

    @Getter
    private final AccountBalance accountBalance;


    public AccountBalanceEvent(final AccountBalance accountBalance) {
        this.accountBalance = accountBalance;
    }

}

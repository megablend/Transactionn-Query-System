package com.nibss.tqs.corporatelounge.queue;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.Organization;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Data
public class AccountProfiling implements Serializable {

    private Account[] successful;
    private Account[] failed;

    private Organization organization;
}
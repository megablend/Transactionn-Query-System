package com.nibss.tqs.corporatelounge.ajax;

import com.nibss.corporatelounge.dto.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by eoriarewo on 10/11/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusChangeDto implements Serializable {

    private long[] acctIds;
    private AccountStatus status;


}

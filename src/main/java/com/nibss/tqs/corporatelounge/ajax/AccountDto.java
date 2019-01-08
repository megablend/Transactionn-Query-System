package com.nibss.tqs.corporatelounge.ajax;

import com.nibss.corporatelounge.dto.AccountStatus;
import com.nibss.corporatelounge.dto.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by eoriarewo on 10/4/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode( of = "id")
public class AccountDto implements Serializable {

    private long id;
    private String accountName;
    private String accountNumber;
    private AccountStatus accountStatus;
    private String bankName;

    private String email;

    private Date dateActive;
    private Date expiryDate;

    private PaymentMode paymentMode;
}

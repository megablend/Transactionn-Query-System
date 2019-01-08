package com.nibss.tqs.ebillspay.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by eoriarewo on 7/28/2016.
 */
@Data
public class AccountDetail implements Serializable {

    private  String accountName;
    private String accounNumber;
    private  String bankCode;
    private  String sortCode;

}

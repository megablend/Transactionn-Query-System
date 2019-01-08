package com.nibss.tqs.ajax;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by eoriarewo on 4/10/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantRegistrationDTO implements Serializable {

    private String aggregatorCode;
    private String aggregatorName;

    private List<String> merchantCodes;
}

package com.nibss.tqs.corporatelounge.ajax;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 10/23/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceEnquiryReportDto implements Serializable {

    private Date startDate;
    private Date endDate;

    private List<AccountBalanceDto> accounts;
}

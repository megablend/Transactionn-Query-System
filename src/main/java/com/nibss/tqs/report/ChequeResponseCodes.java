package com.nibss.tqs.report;

import java.util.stream.Stream;

/**
 * Created by eoriarewo on 8/1/2017.
 */
public enum ChequeResponseCodes {


    RETURNED_CHEQUE ("09","Cheque Returned"),
    AWAITING_CLEARING("08","Awaiting Clearing"),
    CLEARED_CHEQUE ("00", "Cheque Cleared"),
    PRESENTED_CHEQUE ("07", "Cheque Presented"),
    FORMAT_ERROR("05","Format Error"),
    DATABASE_ERROR ("06","Database Error");

    ChequeResponseCodes(String responseCode, String responseDesc) {
        this.responseCode = responseCode;
        this.responseDesc = responseDesc;
    }

    private String responseCode;
    private String responseDesc;


    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseDesc() {
        return responseDesc;
    }

    public static synchronized String getDescriptionForCode(String code) {
        return Stream.of(ChequeResponseCodes.values())
                .filter( e -> e.getResponseCode().equals(code))
                .map( e -> e.getResponseDesc()).findFirst().orElse(null);
    }
}

package com.nibss.tqs.report;

import java.util.stream.Stream;

/**
 * Created by eoriarewo on 9/8/2016.
 */
public enum NipResponseCodes {

        FAILED("01", "Failed"),
        SUCCESSFUL("00", "Approved or Completed Successfully"),
        INVALID_SENDER("03", "Invalid Sender"),
        DO_NOT_HONOR("05", "Do not honor"),
        DORMANT_ACCOUNT("06", "Dormant Account"),
        INVALID_ACCOUNT("07", "Invalid Account"),
        ACCOUNT_NAME_MISMATCH("08", "Account Name Mismatch"),
        REQUEST_PROCESSING_IN_PROGRESS("09", "Request processing in progress"),
        REQUEST_INVALID_TRANSACTION("12", "Invalid transaction"),
        REQUEST_INVALID_AMOUNT("13", "Invalid Amount"),
        INVALID_BATCH_NUMBER("14", "Invalid Batch Number"),
        SESSION_ID("15", "Invalid Session or Record ID"),
        UNKNOWN_BANK_CODE("16", "Unknown Bank Code"),
        INVALID_CHANNEL("17", "Invalid Channel"),
        WRONG_METHOD_CALL("18", "Wrong Method Call"),
        NO_ACTION_TAKEN("21", "No action taken"),
        UNABLE_TO_LOCATE_RECORD("25", "Unable to locate record"),
        DUPLICATE_RECORD("26", "Duplicate record"),
        FORMAT_ERROR("30", "Format error"),
        SUSPECTED_FRAUD("34", "Suspected fraud"),
        CONTACT_MESSAGE_SOURCE("35", "Contact Message Source"),
        INSUFFICIENT_FUNDS("51", "No sufficient funds"),
        TRANSACTION_NOT_PERMITTED_FOR_SENDER("57", "Transaction not permitted to sender"),
        TRANSACTION_NOT_PERMITTED_ON_CHANNEL("58", "Transaction not permitted on channel"),
        TRANSFER_LIMIT_EXCEEDED("61", "Transfer limit Exceeded"),
        SECURITY_VIOLATION("63", "Security violation"),
        EXCEEDS_WITHDRAWAL_FREQUENCY("65", "Exceeds withdrawal frequency"),
        RESPONSE_RECEIVED_TOO_LATE("68", "Response received too late"),
        DESTINATION_NOT_AVAILABLE("91", "Destination not available"),
        ROUTING_ERROR("92", "Routing error"),
        DUPLICATE_TRANSACTION("94", "Duplicate transaction"),
        UNAPPROVED("-1","Unapproved"),
        SYSTEM_MALFUNCTION("96", "System malfunction"),
        TRANSACTION_TIMEOUT("97", "Transaction timeout");

        private String responseCode;
        private String responseDesc;

         NipResponseCodes(String responseCode, String responseDesc) {
            this.responseCode = responseCode;
            this.responseDesc = responseDesc;
        }

        public String getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(String responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponseDesc() {
            return responseDesc;
        }

        public void setResponseDesc(String responseDesc) {
            this.responseDesc = responseDesc;
        }

        public static synchronized  NipResponseCodes findByCode(String responseCode) {
                return Stream.of(NipResponseCodes.values()).filter(t -> t.getResponseCode().equals(responseCode))
                        .findFirst().orElse(null);
        }


        public static synchronized  String getDescriptionForCode(String responseCode) {
               return Stream.of(NipResponseCodes.values())
                        .filter( e -> e.getResponseCode().equals(responseCode))
                       .map( s -> s.getResponseDesc())
                       .findFirst().orElse("");
        }

}

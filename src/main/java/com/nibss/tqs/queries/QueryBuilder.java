package com.nibss.tqs.queries;

import com.nibss.tqs.core.entities.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public interface QueryBuilder {

    /*
    public AjaxEbillsPayTransaction(
           String sessionId,
           BigDecimal amount,
           BigDecimal fee,
           String sourceBankName,
           String destinationBankName,
           String productName,
           Date transactionDate,
           Date dateApproved,
           String responseCode,
           String customerNumber,
           boolean chequeTransaction,
           String chequeResponseCode,
           Date chequeConfirmationDate,
           boolean chequeConfirmed,
           String branchCode

   )
    */
    //

    String EBILLSPAY_BASE_QUERY = "SELECT NEW com.nibss.tqs.ajax.AjaxEbillsPayTransaction" +
            "(t.biller.name,t.sessionId,t.amount,t.transactionFee, t.sourceBank.name,t.destinationBank.name," +
            "t.product.name,t.transactionDate,t.dateApproved,t.responseCode," +
            "t.customerNumber,t.cheque,t.chequeResponse,t.chequeConfirmationDate,t.chequeConfirmationStatus,t.branchCode) FROM BaseTransaction t ";


    /*
   public AjaxCpayCardTransaction(
            String cpayRef,
            String merchantRef,
            String processorId,
            String merchantName,
            String merchantCode,
            BigDecimal amount,
            String productName,
            String responseCode,
            String responseDescription,
            String customerId,
            Date transactionDate,
            Date dateApproved
    )

    */
    String CPAY_CARD_BASE_QUERY = "SELECT NEW com.nibss.tqs.ajax.AjaxCpayCardTransaction(t.cpayRef,t.merchantRef,t.processorId," +
            "t.merchant.name,t.merchant.merchantCode, t.amount, t.product,t.responseCode,t.responseDescription,t.customerId, t.transactionDate, t.dateApproved)" +
            " FROM CardTransaction t ";


    /*
     public AjaxCpayAccountTransaction(
            String merchantName,
            String merchantCode,
            String sourceSessionId,
            String cpayRef,
            String merchantRef,
            String sourceBankName,
            String paymentRef,
            String customerId,
            String product,
            BigDecimal amount,
            BigDecimal fee,
            Date transactionDate,
            String responseCode,
            String processorId
    )
     */
    String CPAY_ACCT_BASE_QUERY = "SELECT new com.nibss.tqs.ajax.AjaxCpayAccountTransaction" +
            "(t.merchant.name,t.merchant.merchantCode, t.sourceSessionId,t.cpayRef,t.merchantRef,t.sourceBank," +
            "t.paymentRef,t.customerId,t.product,t.netAmount,t.fee,t.transactionDate," +
            "t.responseCode, t.processorId) FROM AccountTransaction t LEFT OUTER JOIN t.sourceBank ";



    /*
    public AjaxMcashTransaction(
            String merchantName,
            String merchantCode,
            String telcoName,
            String ussdAggregator,
            String sourceBankName,
            String destinationBankName,
            String sessionId,
            String paymentReference,
            String phoneNumber,
            BigDecimal amount,
            BigDecimal debitFee,
            BigDecimal creditFee,
            Date transactionDate,
            String debitResponseCode,
            String creditResponseCode
    )
     */
    String MCASH_BASE_QUERY = "SELECT NEW com.nibss.tqs.ajax.AjaxMcashTransaction(t.merchant.merchantName,t.merchant.merchantCode," +
            "t.telco.telcoName,t.aggregator.aggregatorName,t.institution.institutionName," +
            "t.sessionID,t.paymentReference,t.referenceCode,t.payerPhoneNumber," +
            "t.amount,t.fee,t.transactionDate," +
            "t.responseCode,t.creditTransaction) FROM DebitTransaction t LEFT OUTER JOIN t.creditTransaction"; //LEFT OUTER JOIN FETCH t.creditTransaction


    /*
    public AjaxUssdTransaction(
            String id,
            String merchantName,
            String merchantCode,
            String ussdAggregator,
            String sessionId,
            String destinationSessionId,
            String phoneNumber,
            String telcoName,
            String sourceBankCode,
            BigDecimal amount,
            BigDecimal fee,
            Date requestTime,
            String debitResponseCode,
            String creditResponseCode
    )
     */
    String USSD_BASE_QUERY = "SELECT NEW com.nibss.tqs.ajax.AjaxUssdTransaction(t.id,t.ussdBiller.name," +
            "t.ussdBiller.merchantCode,t.ussdAggregator.name,t.sourceSessionId,t.destinationSessionId," +
            "t.phoneNumber, t.telco.name,t.sourceBankCode, t.amount,t.transactionFee,t.requestDate," +
            "t.sourceResponseCode, t.destinationResponseCode) FROM UssdTransaction t ";
    /**
     * label of the primary search key for merchant/biller
     * For billers, it holds biller id, form cpay merchants, this is the merchantCode
     */
    String SEARCH_ID ="searchId";

    String DATE_PARAM_NAME = "dateType";

    QueryDTO countQuery(User user, Map<String, String[]> requestMap);

    QueryDTO transactionsQuery(User user,final Map<String,String[]> requestMap);

    default List<String> getRequestMapValue(String key, Map<String, String[]> requestMap) {
        if(requestMap.containsKey(key)) {
           String[] item =  requestMap.get(key);
            if( null != item && item.length > 0)
                return Arrays.asList(item);
            return null;
        }
        return null;
    }

    default String getFormattedStartDate(Date startDate) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        if( startDate == null) {
            Date dDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(dDate);
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            dDate = cal.getTime();
            return  fmt.format(dDate);
        }
        return fmt.format(startDate);
    }


    default String getFormattedEndDate(Date endDate) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        if( endDate == null) {
            Date dDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(dDate);
            cal.set(Calendar.HOUR_OF_DAY,23);
            cal.set(Calendar.MINUTE,59);
            cal.set(Calendar.SECOND,59);
            dDate = cal.getTime();
            return  fmt.format(dDate);
        }
        return fmt.format(endDate);
    }
}

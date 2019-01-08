package com.nibss.tqs.ussd.query;

import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.renderer.TransactionRenderer;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by eoriarewo on 9/6/2016.
 */
public class MerchantPaymentQueryHelper {

    private final static String DATE_APPROVED = "dateApproved";
    private final static String DATE_INITIATED = "dateInitiated";


    //050875B0-3FE3-8BB8-F24D-B92395D8F031
    private final static Pattern USSD_REF_PATTERN = Pattern.compile("(USSD/)(\\d*)(/*)(\\d*)",Pattern.CASE_INSENSITIVE);
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("\\d{30}");
    private static final Pattern MERCHANT_CODE_PATTERN = Pattern.compile("\\d{8}");

    public static final  Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public String buildCommonQueryPart(Map<String, String[]> requestMap, Map<String, Object> paramMap) {
        StringBuilder builder = new StringBuilder();


        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String startDateTime = getRequestParam("startDate", requestMap);
        LocalDateTime defaultStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        startDateTime = startDateTime == null ? defaultStart.format(dateTimeFormatter) : startDateTime;


        String endDateTime = getRequestParam("endDate",requestMap);
        endDateTime = endDateTime == null ? LocalDateTime.now().format(dateTimeFormatter) : endDateTime;


        try {
            DateFormat fmt = new SimpleDateFormat(TransactionRenderer.DATE_FORMAT_PATTERN, Locale.ENGLISH);
            paramMap.put("startDate",fmt.parse(startDateTime));
            paramMap.put("endDate", fmt.parse(endDateTime));
        } catch (ParseException e) {
            paramMap.put("startDate",new Date());
            paramMap.put("endDate", new Date());
        }


        String dateType = getRequestParam(QueryBuilder.DATE_PARAM_NAME, requestMap);
        dateType = dateType == null ? "dateApproved" : dateType;

        if(dateType.equalsIgnoreCase(DATE_APPROVED)) {
            builder.append(" t.transactionDate BETWEEN :startDate AND :endDate");
        } else
            builder.append(" t.transactionDate BETWEEN :startDate AND :endDate");

        String billerId = getRequestParam(QueryBuilder.SEARCH_ID, requestMap);

        if( billerId != null && !billerId.isEmpty()) {
            try {
                paramMap.put("billerId",Long.parseLong(billerId));
                builder.append( " AND t.merchant.merchantId = :billerId");
            } catch (NumberFormatException e) {}
        }

        String search = getRequestParam("search[value]",requestMap);

        if( null != search && !search.isEmpty()) {
            if( SESSION_ID_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.sessionID=:sessionId");
                paramMap.put("sessionId", search);
            } else if (USSD_REF_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.paymentReference = :ussdRef");
                paramMap.put("ussdRef",search);
            } else if(MERCHANT_CODE_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.merchant.merchantCode = :dMerchantCode");
                paramMap.put("dMerchantCode",search);
            } else if (BillPaymentQueryHelper.PHONE_NUMBER_PATTERNS.stream().anyMatch( p -> p.matcher(search).matches()) ) {
                builder.append(" AND t.payerPhoneNumber = :dPhoneNumber");
                paramMap.put("dPhoneNumber",search);
            }else {
                builder.append(" AND t.merchant.merchantCode = :dMerchantCode");
                paramMap.put("dMerchantCode",search);
            }
        }

        return builder.toString();
    }

    public String buildCommonMerchantListPart(Map<String, String[]> requestMap, Map<String, Object> paramMap) {
        StringBuilder builder = new StringBuilder();


        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String startDateTime = getRequestParam("startDate", requestMap);
        LocalDateTime defaultStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        startDateTime = startDateTime == null ? defaultStart.format(dateTimeFormatter) : startDateTime;


        String endDateTime = getRequestParam("endDate",requestMap);
        endDateTime = endDateTime == null ? LocalDateTime.now().format(dateTimeFormatter) : endDateTime;


        try {
            DateFormat fmt = new SimpleDateFormat(TransactionRenderer.DATE_FORMAT_PATTERN, Locale.ENGLISH);
            paramMap.put("startDate",fmt.parse(startDateTime));
            paramMap.put("endDate", fmt.parse(endDateTime));
        } catch (ParseException e) {
            paramMap.put("startDate",new Date());
            paramMap.put("endDate", new Date());
        }

        builder.append(" m.createdDate BETWEEN :startDate AND :endDate");

        String search = getRequestParam("search[value]",requestMap);
        if( null != search && !search.trim().isEmpty()) {
            if(MERCHANT_CODE_PATTERN.matcher(search).matches()) {
                builder.append(" AND m.merchantCode = :merchantCode");
                paramMap.put("merchantCode",search);
            } else if (BillPaymentQueryHelper.PHONE_NUMBER_PATTERNS.stream().anyMatch( p -> p.matcher(search).matches()) ) {
                builder.append(" AND m.phoneNumber = :phoneNumber");
                paramMap.put("phoneNumber",search);
            } else if(EMAIL_PATTERN.matcher(search).matches()) {
                builder.append(" AND m.email = :email");
                paramMap.put("email",search);
            }
        }

        return builder.toString();
    }

    private String getRequestParam(String key, Map<String, String[]> requestMap) {

        if( !requestMap.containsKey(key))
            return null;
        String[] value = requestMap.get(key);
        if( null != value && value.length > 0)
            return value[0].trim();
        return null;

    }
}

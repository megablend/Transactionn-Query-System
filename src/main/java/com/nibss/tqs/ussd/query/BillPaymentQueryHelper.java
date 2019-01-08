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
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by eoriarewo on 9/5/2016.
 */
public class BillPaymentQueryHelper {

    private final static String DATE_APPROVED = "dateApproved";
    private final static String DATE_INITIATED = "dateInitiated";


    //050875B0-3FE3-8BB8-F24D-B92395D8F031
    private final static Pattern USSD_REF_PATTERN = Pattern.compile("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}$");
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("\\d{30}");

    public static final List<Pattern> PHONE_NUMBER_PATTERNS = Arrays.asList( Pattern.compile("\\d{11}"),
            Pattern.compile("234\\d{10}"), Pattern.compile("\\+234\\d{10}"));


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
            builder.append(" t.requestDate BETWEEN :startDate AND :endDate");
        } else
            builder.append(" t.requestDate BETWEEN :startDate AND :endDate");

        String billerId = getRequestParam(QueryBuilder.SEARCH_ID, requestMap);

        if( billerId != null && !billerId.isEmpty()) {
            paramMap.put("billerId",billerId);
            builder.append( " AND t.ussdBiller.merchantCode = :billerId");
        }

        String search = getRequestParam("search[value]",requestMap);

        if( null != search && !search.isEmpty()) {
            if( USSD_REF_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.id=:transactionId");
                paramMap.put("transactionId", search);
            } else if( SESSION_ID_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.sourceSessionId=:sourceSessionId");
                paramMap.put("sourceSessionId", search);
            } else if (PHONE_NUMBER_PATTERNS.stream().anyMatch( p -> p.matcher(search).matches())) {
                builder.append(" AND t.phoneNumber = :phoneNumber");
                paramMap.put("phoneNumber", search);
            } else {
                BigDecimal searchAmount = null;
                try {
                    searchAmount = new BigDecimal(search.replace(",",""));
                }catch(NumberFormatException e) {

                }

                if( null != searchAmount) {
                    builder.append(" AND (t.amount = :amount) ");
                    paramMap.put("amount",searchAmount);
                }
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

package com.nibss.tqs.centralpay.queries;

import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.renderer.EbillsTransactionRenderer;

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
public class CpayAccountQueryHelper {

    private final static String DATE_APPROVED = "dateApproved";
    private final static String DATE_INITIATED = "dateInitiated";


    //00072-055E5-093E9-87847
    private final static Pattern CPAY_REF_PATTERN = Pattern.compile("\\w{5}-\\w{5}-\\w{5}-\\w{5}$");
    private final static Pattern SESSION_ID_PATTERN = Pattern.compile("\\d{30}");

    public String buildCommonQueryPart(Map<String, String[]> requestMap, Map<String, Object> paramMap) {
        StringBuilder builder = new StringBuilder();


        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String startDateTime = getRequestParam("startDate", requestMap);
        LocalDateTime defaultStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        startDateTime = startDateTime == null ? defaultStart.format(dateTimeFormatter) : startDateTime;


        String endDateTime = getRequestParam("endDate", requestMap);
        endDateTime = endDateTime == null ? LocalDateTime.now().format(dateTimeFormatter) : endDateTime;


        try {
            DateFormat fmt = new SimpleDateFormat(EbillsTransactionRenderer.DATE_FORMAT_PATTERN, Locale.ENGLISH);
            paramMap.put("startDate", fmt.parse(startDateTime));
            paramMap.put("endDate", fmt.parse(endDateTime));
        } catch (ParseException e) {
            paramMap.put("startDate", new Date());
            paramMap.put("endDate", new Date());
        }


       /* String dateType = getRequestParam(QueryBuilder.DATE_PARAM_NAME, requestMap);
        dateType = dateType == null ? "dateApproved" : dateType;

        if(dateType.equalsIgnoreCase(DATE_APPROVED)) {
            builder.append(" t.dateApproved BETWEEN :startDate AND :endDate");
        } else*/
        builder.append(" t.transactionDate BETWEEN :startDate AND :endDate");

        String billerId = getRequestParam(QueryBuilder.SEARCH_ID, requestMap);

        if (billerId != null && !billerId.trim().isEmpty()) {
            paramMap.put("billerId", billerId.trim());
            builder.append(" AND t.merchant.merchantCode = :billerId");
        }

        String search = getRequestParam("search[value]", requestMap);

        if (null != search && !search.trim().isEmpty()) {

            if (CPAY_REF_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.cpayRef=:cpayRef");
                paramMap.put("cpayRef", search);
            } else if (SESSION_ID_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.sourceSessionId=:sessionId");
                paramMap.put("sessionId", search);
            } else {
                BigDecimal searchAmount = null;
                try {
                    searchAmount = new BigDecimal(search.replace(",", ""));
                } catch (NumberFormatException e) {

                }

                if (null != searchAmount) {
                    builder.append(" AND (t.amount = :amount) ");
                    paramMap.put("amount", searchAmount);
                } else {
                    search = "%" + search + "%";
                    builder.append(
                            " AND (t.cpayRef LIKE :search OR t.sourceSessionId LIKE :search OR t.sourceBank.name LIKE :search OR t.merchantRef LIKE :search)");
                    paramMap.put("search", search);
                }
            }
        }

        return builder.toString();
    }

    private String getRequestParam(String key, Map<String, String[]> requestMap) {

        if (!requestMap.containsKey(key))
            return null;
        String[] value = requestMap.get(key);
        if (null != value && value.length > 0)
            return value[0].trim();
        return null;

    }
}

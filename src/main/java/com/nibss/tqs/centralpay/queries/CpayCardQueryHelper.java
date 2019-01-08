package com.nibss.tqs.centralpay.queries;

import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.renderer.EbillsTransactionRenderer;

import java.io.Serializable;
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
 * Created by eoriarewo on 9/2/2016.
 */
public class CpayCardQueryHelper implements Serializable {

    private final static String DATE_APPROVED = "dateApproved";
    private final static String DATE_INITIATED = "dateInitiated";

    private List<String> INTERSWITCH = Arrays.asList("ISW", "INTERSWITCH", "INTER SWITCH", "INTER-SWITCH");
    private List<String> UNIFIED_PAYMENTS = Arrays.asList("UPSL", "UNIFIED", "UNIFIED PAYMENTS", "UNIFIED PAYMENTS SERVICES LIMITED");

    //00072-055E5-093E9-87847
    private final static Pattern CPAY_REF_PATTERN = Pattern.compile("\\w{5}-\\w{5}-\\w{5}-\\w{5}$");

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


   /*     String dateType = getRequestParam(QueryBuilder.DATE_PARAM_NAME, requestMap);
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
            if (CPAY_REF_PATTERN.matcher(search.trim()).matches()) {
                builder.append(" AND t.cpayRef=:cpayRef");
                paramMap.put("cpayRef", search.trim());
            } else {

                if (INTERSWITCH.stream().anyMatch(s -> s.equalsIgnoreCase(search))) {
                    builder.append(" AND t.paymentGateway.name=:paymentGateway ");
                    paramMap.put("paymentGateway", "ISW");
                } else if (UNIFIED_PAYMENTS.stream().anyMatch(s -> s.equalsIgnoreCase(search))) {
                    builder.append(" AND t.paymentGateway.name=:paymentGateway ");
                    paramMap.put("paymentGateway", "UPSL");
                } else if (search.equalsIgnoreCase("NIBSS")) {
                    builder.append(" AND t.paymentGateway.name=:paymentGateway ");
                    paramMap.put("paymentGateway", "NIBSS");
                } else {
                    String orSearch = "%" + search + "%";
                    builder.append(" AND (t.cpayRef LIKE :search OR t.merchantRef LIKE :search OR t.merchant.name LIKE :search)");
                    paramMap.put("search", orSearch);
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

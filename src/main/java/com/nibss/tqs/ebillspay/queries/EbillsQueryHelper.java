package com.nibss.tqs.ebillspay.queries;

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
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by eoriarewo on 7/8/2016.
 */
public class EbillsQueryHelper {

    private final static String DATE_APPROVED = "dateApproved";
    private final static String DATE_INITIATED = "dateInitiated";

    private final static Pattern SESSION_ID_PATTERN = Pattern.compile("\\d{30}$");

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

        String dateType = getRequestParam(QueryBuilder.DATE_PARAM_NAME, requestMap);
        dateType = dateType == null ? "dateApproved" : dateType;

        if (dateType.equalsIgnoreCase(DATE_APPROVED)) {
            builder.append(" (t.dateApproved BETWEEN :startDate AND :endDate OR t.chequeConfirmationDate BETWEEN :startDate AND :endDate) ");
        } else {
            builder.append(" t.transactionDate BETWEEN :startDate AND :endDate");
        }

        String billerId = getRequestParam(QueryBuilder.SEARCH_ID, requestMap);

        if (billerId != null && !billerId.trim().isEmpty()) {
            try {
                paramMap.put("billerId", Integer.parseInt(billerId.trim()));
                builder.append(" AND t.biller.id = :billerId");
            } catch (NumberFormatException e) {
            }

        }

        String search = getRequestParam("search[value]", requestMap);

        if (null != search && !search.trim().isEmpty()) {
            if (SESSION_ID_PATTERN.matcher(search).matches()) {
                builder.append(" AND t.sessionId=:sessionId");
                paramMap.put("sessionId", search);
            } else {
                search = "%" + search + "%";
                builder.append(" AND (t.customerNumber LIKE :search OR t.biller.name LIKE :search OR t.sessionId LIKE :search )");
                paramMap.put("search", search);
            }
        }

        return builder.toString();
    }

    private String getRequestParam(String key, Map<String, String[]> requestMap) {

        if (!requestMap.containsKey(key)) {
            return null;
        }
        String[] value = requestMap.get(key);
        if (null != value && value.length > 0) {
            return value[0];
        }
        return null;

    }
}

package com.nibss.tqs.centralpay.queries;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eoriarewo on 9/3/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NibssCpayAccountQueryBuilder implements QueryBuilder {

    private static final String COUNT_QUERY = "SELECT COUNT(t) FROM AccountTransaction t ";
//    private static final String TXN_QUERY = "SELECT t FROM AccountTransaction t ";
    private static final String TXN_QUERY = CPAY_ACCT_BASE_QUERY;

    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,COUNT_QUERY,requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,TXN_QUERY,requestMap);
    }

    private QueryDTO buildResponseDTO( final User user, final String baseQuery, Map<String, String[]> requestMap) {
        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append( " WHERE " + new CpayAccountQueryHelper().buildCommonQueryPart(requestMap,paramMap));
        builder.append(" ORDER BY t.transactionDate DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

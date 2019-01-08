package com.nibss.tqs.centralpay.queries;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by eoriarewo on 9/3/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AggregatorCpayAccountQueryBuilder implements QueryBuilder {

    private final static String COUNT_QUERY =" SELECT COUNT(t) FROM AccountTransaction t ";
//    private final static String TRXN_QUERY = "SELECT t FROM AccountTransaction t ";
    private final static String TRXN_QUERY = CPAY_ACCT_BASE_QUERY;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,COUNT_QUERY, requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,TRXN_QUERY, requestMap);
    }

    private QueryDTO buildResponseDTO( final User user, final String baseQuery, Map<String, String[]> requestMap) {

        List<String> merchantCodes = Arrays.asList("UNSTSTSETVTWWW");

        Collection<String> codes = organizationRepository.findCentralPayMerchantCodesForOrganization(user.getOrganizationInterface().getId());

        if( null != codes && !codes.isEmpty())
            merchantCodes = new ArrayList<>(codes);

        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append(" WHERE t.merchant.merchantCode IN :merchantCodes");
        paramMap.put("merchantCodes",merchantCodes);
        builder.append( " AND " + new CpayAccountQueryHelper().buildCommonQueryPart(requestMap,paramMap));
        builder.append(" ORDER BY t.transactionDate DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

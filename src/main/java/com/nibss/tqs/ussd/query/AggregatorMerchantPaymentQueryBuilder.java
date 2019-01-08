package com.nibss.tqs.ussd.query;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 9/6/2016.
 */
@Component
@Scope(value = "prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AggregatorMerchantPaymentQueryBuilder implements QueryBuilder {

    private final static String COUNT_QUERY = "SELECT COUNT(t) FROM DebitTransaction t ";
//     private final static String TXN_QUERY = "SELECT t FROM DebitTransaction t LEFT OUTER JOIN FETCH t.creditTransaction ";
     private final static String TXN_QUERY = MCASH_BASE_QUERY;


     @Autowired
     private OrganizationRepository orgRepo;

    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,COUNT_QUERY,requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,TXN_QUERY,requestMap);
    }

    private QueryDTO buildResponseDTO( final User user, final String baseQuery, Map<String, String[]> requestMap) {
        Collection<Number> merIds   = orgRepo.findMerchantIdsByOrganization(user.getOrganizationInterface().getId());

        List<Long> merchantIds = Arrays.asList(-5690L);
        if( null != merIds && !merIds.isEmpty())
            merchantIds = merIds.stream().map( i -> i.longValue()).collect(Collectors.toList());

        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append(" WHERE t.merchant.merchantId IN :myMerchantIds");
        paramMap.put("myMerchantIds",merchantIds);
        builder.append( " AND " + new MerchantPaymentQueryHelper().buildCommonQueryPart(requestMap,paramMap));
        builder.append(" ORDER BY t.transactionDate DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

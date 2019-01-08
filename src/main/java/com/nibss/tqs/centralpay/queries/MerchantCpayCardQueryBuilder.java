package com.nibss.tqs.centralpay.queries;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by eoriarewo on 9/3/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MerchantCpayCardQueryBuilder implements QueryBuilder {

    private static  final String UNREGISTERED = "XXXXYYYYYYY473020280";

    private static final String COUNT_QUERY = "SELECT COUNT(t) FROM CardTransaction t";

    private static final String TRXN_QUERY = CPAY_CARD_BASE_QUERY;

    @Autowired
    private OrganizationRepository orgRepo;



    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,COUNT_QUERY,requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,TRXN_QUERY,requestMap);
    }

    private QueryDTO buildResponseDTO( final User user, final String baseQuery, Map<String, String[]> requestMap) {

        String merchantCode = UNREGISTERED;

        Collection<String> codes = orgRepo.findCentralPayMerchantCodesForOrganization(user.getOrganizationInterface().getId());

        if( null != codes && !codes.isEmpty())
            merchantCode = codes.iterator().next();

        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append(" WHERE t.merchant.merchantCode=:billerMerchantCode");
        paramMap.put("billerMerchantCode",merchantCode);
        builder.append( " AND " + new CpayCardQueryHelper().buildCommonQueryPart(requestMap,paramMap));
        builder.append(" ORDER BY t.transactionDate DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

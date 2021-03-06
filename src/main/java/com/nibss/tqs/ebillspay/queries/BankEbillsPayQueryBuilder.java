package com.nibss.tqs.ebillspay.queries;

import com.nibss.tqs.core.entities.Bank;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Emor on 8/4/2016.
 */
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BankEbillsPayQueryBuilder implements QueryBuilder {

    //    private final String TRXN_QUERY = "SELECT t FROM BaseTransaction t ";
    private final String TRXN_QUERY = EBILLSPAY_BASE_QUERY;

    private final String COUNT_QUERY = "SELECT COUNT(t) FROM BaseTransaction t ";


    @Autowired
    private BankRepository bankRepository;

    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user, COUNT_QUERY, requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user, TRXN_QUERY, requestMap);
    }

    private QueryDTO buildResponseDTO(final User user, final String baseQuery, Map<String, String[]> requestMap) {
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append(" WHERE t.sourceBank.code=:bankCode ");
       /* if( !userBank.getOrganizationSetting().isEbillspayTransactionDateAllowed())
            builder.append( " AND (t.responseCode != '-1' OR t.responseCode IS NOT NULL )");*/

        paramMap.put("bankCode", bankRepository.findCbnCodeByBank(user.getOrganizationInterface().getId()));
        builder.append(" AND " + new EbillsQueryHelper().buildCommonQueryPart(requestMap, paramMap));
        builder.append(" ORDER BY t.id DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

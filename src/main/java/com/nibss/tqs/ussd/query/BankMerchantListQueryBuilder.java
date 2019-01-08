package com.nibss.tqs.ussd.query;

import com.nibss.tqs.core.entities.Bank;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eoriarewo on 3/28/2017.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BankMerchantListQueryBuilder implements QueryBuilder {

    private final static String COUNT_QUERY = "SELECT COUNT(m) FROM Merchant m ";
    private final static String TXN_QUERY = "SELECT m FROM Merchant m  JOIN FETCH m.merchantAccountList ";


    @Autowired
    private BankRepository bankRepository;

    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,COUNT_QUERY, requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,TXN_QUERY, requestMap);
    }

    private QueryDTO buildResponseDTO( final User user, final String baseQuery, Map<String, String[]> requestMap) {
        String bankCode = "DOESNOTEXIST";

        String temp = bankRepository.findNipCodeByBank(user.getOrganizationInterface().getId());
        if( null != temp)
            bankCode = temp;

        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append(" WHERE m.aggregator.aggregatorCode = :aggregatorCode");
        paramMap.put("aggregatorCode",bankCode);
        builder.append( " AND " + new MerchantPaymentQueryHelper().buildCommonMerchantListPart(requestMap,paramMap));
        builder.append(" ORDER BY m.merchantId DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

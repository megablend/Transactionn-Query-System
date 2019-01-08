package com.nibss.tqs.ussd.query;

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
 * Created by eoriarewo on 9/6/2016.
 */
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BankBillPaymentQueryBuilder implements QueryBuilder {

    private final static String COUNT_QUERY = "SELECT COUNT(t) FROM UssdTransaction t ";
//    private final static String TXN_QUERY = "SELECT t FROM UssdTransaction t ";
    private final static String TXN_QUERY = USSD_BASE_QUERY;

    @Autowired
    private BankRepository bankRepository;

    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,COUNT_QUERY,requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return buildResponseDTO(user,TXN_QUERY,requestMap);
    }

    private QueryDTO buildResponseDTO( final User user, final String baseQuery, Map<String, String[]> requestMap) {
        String bankCode = "$$@12";
        bankCode = bankRepository.findCbnCodeByBank(user.getOrganizationInterface().getId());
        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append(" WHERE t.sourceBankCode=:myBankCode");
        paramMap.put("myBankCode",bankCode);
        builder.append( " AND " + new BillPaymentQueryHelper().buildCommonQueryPart(requestMap,paramMap));
        builder.append(" ORDER BY t.requestDate DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

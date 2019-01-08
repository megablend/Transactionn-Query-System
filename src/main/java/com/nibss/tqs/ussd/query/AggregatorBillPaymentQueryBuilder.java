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

/**
 * Created by eoriarewo on 9/6/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AggregatorBillPaymentQueryBuilder implements QueryBuilder {


    private static final String COUNT_QUERY = "SELECT COUNT(t) FROM UssdTransaction t ";
//    private static final String TXN_QUERY = "SELECT t FROM UssdTransaction t ";
    private static final String TXN_QUERY = USSD_BASE_QUERY;

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
        List<String> merchantCodes = Arrays.asList("XXXSSSS");
        Collection<String> ussdCodes = orgRepo.findUssdBillerCodesForOrganization(user.getOrganizationInterface().getId());
        if( null != ussdCodes && !ussdCodes.isEmpty())
            merchantCodes = new ArrayList<>(ussdCodes);

        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        builder.append(" WHERE t.ussdBiller.merchantCode IN :myMerchantCodes");
        paramMap.put("myMerchantCodes",merchantCodes);
        builder.append( " AND " + new BillPaymentQueryHelper().buildCommonQueryPart(requestMap,paramMap));
        builder.append(" ORDER BY t.requestDate DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }
}

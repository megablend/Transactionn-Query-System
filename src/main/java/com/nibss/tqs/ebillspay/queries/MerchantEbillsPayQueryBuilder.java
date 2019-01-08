package com.nibss.tqs.ebillspay.queries;

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
 * Created by eoriarewo on 7/8/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MerchantEbillsPayQueryBuilder implements QueryBuilder {

//    private final String TRXN_QUERY = "SELECT t FROM BaseTransaction t ";
    private final String TRXN_QUERY = EBILLSPAY_BASE_QUERY;

    private  final String COUNT_QUERY  ="SELECT COUNT(t) FROM BaseTransaction t";

    public final static int UNREGISTERED = -99;

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
        Map<String,Object> paramMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append( getBasicQueryForUser(user,paramMap,baseQuery));
        /*if( !user.getOrganization().getOrganizationSetting().isEbillspayTransactionDateAllowed())
            builder.append( " AND (t.responseCode != '-1' OR t.responseCode IS NOT NULL)");*/

        builder.append( " AND " + new EbillsQueryHelper().buildCommonQueryPart(requestMap,paramMap));
        builder.append(" ORDER BY t.id DESC");

        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);
        return dto;
    }

    private String getBasicQueryForUser(final User user, Map<String,Object> paramMap, String baseQuery) {


        Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(user.getOrganizationInterface().getId());
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        if( null == billerIds || billerIds.isEmpty()) {
            paramMap.put("billerId", UNREGISTERED);
        } else {
            int billerId = billerIds.iterator().next();
            paramMap.put("billerId", billerId);
        }

        builder.append( " WHERE t.biller.id = :billerId " );
        return builder.toString();
    }
}

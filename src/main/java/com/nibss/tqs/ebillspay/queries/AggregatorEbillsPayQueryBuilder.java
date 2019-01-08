package com.nibss.tqs.ebillspay.queries;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import javafx.scene.effect.SepiaTone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AggregatorEbillsPayQueryBuilder implements QueryBuilder {

    private final static String COUNT_QUERY = "SELECT COUNT(t) FROM BaseTransaction t WHERE t.biller.id IN :billerIds ";

//    private final static String TRXN_QUERY = "SELECT t FROM BaseTransaction t WHERE t.biller.id IN :billerIds ";
    private final static String TRXN_QUERY = EBILLSPAY_BASE_QUERY + " WHERE t.biller.id IN :billerIds ";

    @Autowired
    private OrganizationRepository orgRepo;

    @Override
    public QueryDTO countQuery(User user,Map<String, String[]> requestMap) {
        return buildBasicQueryDTO(user, COUNT_QUERY,requestMap);
    }

    @Override
    public QueryDTO transactionsQuery(User user,Map<String, String[]> requestMap) {
        return buildBasicQueryDTO(user, TRXN_QUERY,requestMap);
    }

    private QueryDTO buildBasicQueryDTO(final User user, String baseQuery, Map<String,String[]> requestMap) {
        Map<String,Object> paramMap = buildBasicParamMap(user,requestMap);
        StringBuilder builder = new StringBuilder();
        builder.append(baseQuery);
        /*if(!user.getOrganization().getOrganizationSetting().isEbillspayTransactionDateAllowed())
            builder.append(" AND (t.responseCode != '-1' OR t.responseCode IS NOT NULL) ");*/

        builder.append( " AND " + new EbillsQueryHelper().buildCommonQueryPart(requestMap, paramMap));
        builder.append(" ORDER BY t.id DESC");
        QueryDTO dto = new QueryDTO();
        dto.setQuery(builder.toString());
        dto.setParameters(paramMap);

        return dto;
    }

    private Map<String,Object> buildBasicParamMap(final User user,Map<String, String[]> requestMap) {
        Map<String,Object> paramMap = new HashMap<>();
        Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(user.getOrganizationInterface().getId());
        paramMap.put("billerIds",billerIds == null ? new ArrayList<>() : billerIds);
        return paramMap;
    }

}

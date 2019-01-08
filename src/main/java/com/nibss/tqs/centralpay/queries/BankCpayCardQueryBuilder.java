package com.nibss.tqs.centralpay.queries;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.queries.QueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by eoriarewo on 9/3/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BankCpayCardQueryBuilder implements QueryBuilder {

    @Override
    public QueryDTO countQuery(User user, Map<String, String[]> requestMap) {
        return null;
    }

    @Override
    public QueryDTO transactionsQuery(User user, Map<String, String[]> requestMap) {
        return null;
    }
}

package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.Merchant;
import com.nibss.tqs.core.entities.Bank;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.OrganizationType;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.queries.QueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by eoriarewo on 4/6/2017.
 */
@Slf4j
public class MPMerchantRepositoryImpl implements MPMerchantRepositoryRepo {

    private EntityManager entityManager;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    public MPMerchantRepositoryImpl(final JpaContext jpaContext) {
        entityManager = jpaContext.getEntityManagerByManagedType(Merchant.class);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public long totalCount(User user) {
        IOrganization org = user.getOrganizationInterface();
        if( org.getOrganizationType() == OrganizationType.NIBSS_INT)
            return countForNibss();
        else if( org.getOrganizationType() == OrganizationType.BANK_INT)
            return countForBank(org);
        // TODO: 4/6/2017 modify as bank relationship to merchant gets clearer
        return 0;
    }

    private long countForBank(IOrganization org) {
        String bankCode = bankRepository.findNipCodeByBank(org.getId());
        TypedQuery<Long> tq = entityManager.createQuery("SELECT COUNT(m) FROM Merchant m WHERE m.aggregator.aggregatorCode = :aggCode",Long.class);
        tq.setParameter("aggCode",bankCode);
        long count = tq.getSingleResult();
        log.trace("total merchant count for bank {} : {}", bankCode, count);
        return count;
    }


    @Override
    @PreAuthorize("isAuthenticated()")
    public long filteredCount(QueryDTO queryDTO) {
        TypedQuery<Long> tq = entityManager.createQuery(queryDTO.getQuery(), Long.class);
        applyParams(tq, queryDTO.getParameters());
        return tq.getSingleResult();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<Merchant> findMerchants(QueryDTO queryDTO, int start, int itemCount) {
        TypedQuery<Merchant> mQ = entityManager.createQuery(queryDTO.getQuery(), Merchant.class);
        applyParams(mQ, queryDTO.getParameters());
        return mQ.getResultList();
    }


    private long countForNibss() {
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(m) FROM Merchant m",Long.class);
        return query.getSingleResult();

    }

    private void applyParams(final Query query, Map<String, Object> paramMap) {
        if (paramMap != null && !paramMap.isEmpty()) {
            paramMap.forEach((k, v) -> {
                if (v instanceof Date) {
                    query.setParameter(k, (Date) v, TemporalType.TIMESTAMP);
                } else {
                    query.setParameter(k, v);
                }
            });
        }
    }
}

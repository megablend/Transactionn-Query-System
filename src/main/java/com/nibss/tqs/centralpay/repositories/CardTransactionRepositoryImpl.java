package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.ajax.AjaxCpayCardTransaction;
import com.nibss.tqs.centralpay.dto.CardTransaction;
import com.nibss.tqs.centralpay.service.GatewayResponseService;
import com.nibss.tqs.core.entities.OrganizationType;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.queries.QueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by eoriarewo on 7/11/2016.
 */
public class CardTransactionRepositoryImpl implements CardTransactionCustomRepo {

    private final String BASE_QUERY = "SELECT COUNT(c) FROM CardTransaction c ";
    private EntityManager em;

    @Autowired
    private OrganizationRepository orgRepo;

/*    @Autowired
    private GatewayResponseService gatewayResponseService;*/


    @Autowired
    public void setJpaContext(final JpaContext jpaContext) {
        this.em = jpaContext.getEntityManagerByManagedType(CardTransaction.class);
    }

    @Override
    public long filteredCount(QueryDTO queryDTO) {
        TypedQuery<Long> countQuery = em.createQuery(queryDTO.getQuery(), Long.class);
        if (queryDTO.getParameters() != null && !queryDTO.getParameters().isEmpty()) {
            applyParams(countQuery, queryDTO.getParameters());
        }
        return countQuery.getSingleResult();
    }

    @Override
    public long totalCount(User user) {
        IOrganization userOrg = user.getOrganizationInterface();
        int orgType = userOrg.getOrganizationType();

        if (orgType == OrganizationType.NIBSS_INT)
            return countForNibss();
        else if (orgType == OrganizationType.AGGREGATOR_INT)
            return countForAggregator(user);
        else if (orgType == OrganizationType.MERCHANT_INT)
            return countForMerchant(user);
        else
            return 0; //how do we identify card transactions for banks?
    }

    @Override
    public List<AjaxCpayCardTransaction> findTransactions(QueryDTO queryDTO, int start, int itemCount) {
        TypedQuery<AjaxCpayCardTransaction> trxnQuery = em.createQuery(queryDTO.getQuery(), AjaxCpayCardTransaction.class);

        if (queryDTO.getParameters() != null && !queryDTO.getParameters().isEmpty()) {
            applyParams(trxnQuery, queryDTO.getParameters());
        }

        if (start > 0 && itemCount > 0) {
            trxnQuery.setFirstResult(start - 1);
            trxnQuery.setMaxResults(itemCount);
        }

        List<AjaxCpayCardTransaction> result = trxnQuery.getResultList();

      /*  if (null != result) {
            result.stream().forEach(x -> {

                try {
                    String procId = x.getProcessorId() != null ? x.getProcessorId() : AjaxCpayCardTransaction.PROCESSOR_ID;
                    String desc = gatewayResponseService.getDescriptionByResponseCodeAndProcessor(x.getResponseCode(), procId);
                    x.setResponseDescription(desc);
                } catch (Exception e) {
                }
            });
        }*/

        return result;
    }

    private long countForNibss() {
        TypedQuery<Long> tQ = em.createQuery("SELECT COUNT(t) FROM CardTransaction t", Long.class);
        return tQ.getSingleResult();
    }

    private long countForMerchant(User user) {
        Collection<String> codes = orgRepo.findCentralPayMerchantCodesForOrganization(user.getOrganizationInterface().getId());
        if (null == codes || codes.isEmpty())
            return 0;

        TypedQuery<Long> tQ = em.createQuery("SELECT COUNT(t) FROM CardTransaction t WHERE t.merchant.merchantCode=:merchantCode", Long.class);
        tQ.setParameter("merchantCode", codes.iterator().next());
        return tQ.getSingleResult();
    }

    private long countForAggregator(User user) {
        Collection<String> codes = orgRepo.findCentralPayMerchantCodesForOrganization(user.getOrganizationInterface().getId());
        if (null == codes || codes.isEmpty())
            return 0;

        TypedQuery<Long> tQ = em.createQuery("SELECT COUNT(t) FROM CardTransaction t WHERE t.merchant.merchantCode IN :merchantCodes", Long.class);
        tQ.setParameter("merchantCodes", codes);
        return tQ.getSingleResult();
    }

    private void applyParams(final Query query, final Map<String, Object> params) {
        if (null != params && !params.isEmpty()) {
            params.forEach((k, v) -> {
                if (v instanceof Date)
                    query.setParameter(k, (Date) v, TemporalType.TIMESTAMP);
                else
                    query.setParameter(k, v);
            });
        }
    }
}

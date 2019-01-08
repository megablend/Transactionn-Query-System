package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.ajax.AjaxCpayAccountTransaction;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public class AccountTransactionRepositoryImpl implements AccountTransactionCustomRepo {


    private EntityManager em;

    @Autowired
    private GatewayResponseService gatewayResponseService;

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    public void setJpaContext(final JpaContext jpaContext) {
        em = jpaContext.getEntityManagerByManagedType(AccountTransaction.class);
    }

    @Override
    public long filteredCount(QueryDTO queryDTO) {
        TypedQuery<Long> countQuery = em.createQuery(queryDTO.getQuery(), Long.class);
        applyParams(countQuery, queryDTO.getParameters());

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
            return 0; //how do we identify account transactions for banks?
    }

    @Override
    public List<AjaxCpayAccountTransaction> findTransactions(QueryDTO queryDTO, int start, int itemCount) {
        TypedQuery<AjaxCpayAccountTransaction> trxnQuery = em.createQuery(queryDTO.getQuery(), AjaxCpayAccountTransaction.class);

        applyParams(trxnQuery, queryDTO.getParameters());

        if (start > 0 && itemCount > 0) {
            trxnQuery.setFirstResult(start - 1);
            trxnQuery.setMaxResults(itemCount);

        }

        List<AjaxCpayAccountTransaction> result = trxnQuery.getResultList();

        if (null != result) {
            result.stream().forEach(x -> {

                try {
                    String desc = gatewayResponseService.getDescriptionByResponseCodeAndProcessor(x.getResponseCode(), x.getProcessorId());
                    x.setResponseDescription(desc);
                } catch (Exception e) {

                }
            });
        }

        return result;
    }

    @Override
    public List<AccountTransaction> getTransactionsWeeklyBilling() {
        String query = "SELECT t FROM AccountTransaction t JOIN FETCH t.merchant WHERE t.billed <> true AND t.fee > 0 AND t.merchant.id IN " +
                " (SELECT m.merchant.id FROM CpayAccountSharingConfig m) " +
                " AND t.transactionDate BETWEEN :startDate AND :endDate AND t.responseCode = '00' ";

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);

        Date startDate = Timestamp.valueOf(startDateTime);
        Date endDate = Timestamp.valueOf(endDateTime);

        return getTransactions(startDate, endDate, query);
    }

    @Override
    public List<AccountTransaction> getTransactionsForBillingPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        String query = "SELECT t FROM AccountTransaction t JOIN FETCH t.merchant WHERE t.fee > 0 AND t.responseCode = '00' " +
                " AND t.transactionDate BETWEEN :startDate AND :endDate " +
                " AND t.merchant.id IN  (SELECT m.merchant.id FROM CpayAccountSharingConfig m) ";

        Date startDate = Timestamp.valueOf(startDateTime);
        Date endDate = Timestamp.valueOf(endDateTime);

        return getTransactions(startDate, endDate, query);
    }

    private List<AccountTransaction> getTransactions(final Date startDate, final Date endDate, final String query) {
        TypedQuery<AccountTransaction> tQ = em.createQuery(query, AccountTransaction.class);
        return tQ.setParameter("startDate", startDate, TemporalType.TIMESTAMP)
                .setParameter("endDate", endDate, TemporalType.TIMESTAMP)
                .getResultList();
    }

    private long countForNibss() {
        TypedQuery<Long> tq = em.createQuery("SELECT COUNT(c) FROM AccountTransaction c", Long.class);
        return tq.getSingleResult();
    }

    private long countForMerchant(User user) {
        Collection<String> codes = orgRepo.findCentralPayMerchantCodesForOrganization(user.getOrganizationInterface().getId());
        if (null == codes || codes.isEmpty())
            return 0;

        TypedQuery<Long> tQ = em.createQuery("SELECT COUNT(t) FROM AccountTransaction t WHERE t.merchant.merchantCode=:merchantCode", Long.class);
        tQ.setParameter("merchantCode", codes.iterator().next());
        return tQ.getSingleResult();
    }

    private long countForAggregator(User user) {
        Collection<String> codes = orgRepo.findCentralPayMerchantCodesForOrganization(user.getOrganizationInterface().getId());
        if (null == codes || codes.isEmpty())
            return 0;

        TypedQuery<Long> tQ = em.createQuery("SELECT COUNT(t) FROM AccountTransaction t WHERE t.merchant.merchantCode IN :merchantCodes", Long.class);
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

    @Override
    public List<AccountTransaction> getTransactionsForBacklogs() {
        String query = "SELECT t FROM AccountTransaction t JOIN FETCH t.merchant WHERE t.billed <> true AND t.fee > 0 AND t.merchant.id IN " +
                " (SELECT m.merchant.id FROM CpayAccountSharingConfig m) " +
                " AND t.transactionDate <= :date AND t.responseCode = '00' ";

        LocalDateTime start = LocalDateTime.of(LocalDate.now().minusDays(10), LocalTime.MIN);
        Date startDate = Timestamp.valueOf(start);

        TypedQuery<AccountTransaction> tq = em.createQuery(query, AccountTransaction.class);
        tq.setParameter("date", startDate, TemporalType.TIMESTAMP);

        return tq.getResultList();
    }
}

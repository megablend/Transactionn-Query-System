package com.nibss.tqs.merchantpayment;

import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.tqs.ajax.AjaxMcashTransaction;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.BankRepository;
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
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 8/18/2016.
 */
public class MerchantPaymentTransactionRepositoryImpl implements MerchantPaymentTransactionRepo {

    private EntityManager entityManager;
    private OrganizationRepository orgRepo;

    private BankRepository bankRepository;

    @Autowired
    public MerchantPaymentTransactionRepositoryImpl(JpaContext jpaContext, OrganizationRepository orgRepo,
                                                    BankRepository bankRepository) {
        entityManager = jpaContext.getEntityManagerByManagedType(DebitTransaction.class);
        this.orgRepo = orgRepo;
        this.bankRepository = bankRepository;
    }

    @Override
    public List<DebitTransaction> getTransactionsForWeeklyBilling() {

        String query = "SELECT d FROM DebitTransaction d JOIN FETCH d.creditTransaction x WHERE d.responseCode='00' "
                + " AND d.billed <> true AND d.transactionDate BETWEEN ?1 AND ?2 AND (d.fee  > 0 OR d.creditTransaction.fee > 0)";

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);
        return getTransactions(query, startDateTime, endDateTime);
    }

    @Override
    public List<DebitTransaction> getTransactionsForBillingPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        String query = "SELECT d FROM DebitTransaction d JOIN FETCH d.creditTransaction x  WHERE d.responseCode='00' "
                + " AND d.transactionDate BETWEEN ?1 AND ?2 AND (d.fee  > 0 OR d.creditTransaction.fee > 0)";

        if (null == startDateTime || null == endDateTime) {
            throw new NullPointerException("Parameters cannot be null");
        }

        return getTransactions(query, startDateTime, endDateTime);
    }

    private List<DebitTransaction> getTransactions(String query, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Date startDate = Timestamp.valueOf(startDateTime);
        Date endDate = Timestamp.valueOf(endDateTime);

        TypedQuery<DebitTransaction> trxnQ = entityManager.createQuery(query, DebitTransaction.class);
        return trxnQ.setParameter(1, startDate, TemporalType.TIMESTAMP)
                .setParameter(2, endDate, TemporalType.TIMESTAMP).getResultList();
    }

    @Override
    public long totalCount(User user) {
        IOrganization org = user.getOrganizationInterface();

        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                return countForAggregator(org);
            case OrganizationType.NIBSS_INT:
                return countForNibss();
            case OrganizationType.MERCHANT_INT:
                return countForMerchant(org);
            case OrganizationType.BANK_INT:
                return countForBank(org);
            default:
                return 0;
        }

    }

    @Override
    public long filteredCount(QueryDTO queryDTO) {
        TypedQuery<Long> tq = entityManager.createQuery(queryDTO.getQuery(), Long.class);
        applyParams(tq, queryDTO.getParameters());
        return tq.getSingleResult();
    }

    @Override
    public List<AjaxMcashTransaction> findTransactions(QueryDTO queryDTO, int start, int itemCount) {
        TypedQuery<AjaxMcashTransaction> tq = entityManager.createQuery(queryDTO.getQuery(), AjaxMcashTransaction.class);
        applyParams(tq, queryDTO.getParameters());
        return tq.getResultList();
    }

    private long countForNibss() {
        TypedQuery<Long> tq = entityManager.createQuery("SELECT COUNT(t) FROM DebitTransaction t", Long.class);
        return tq.getSingleResult();
    }

    private long countForMerchant(IOrganization org) {
        Collection<Number> merchantIds = orgRepo.findMerchantIdsByOrganization(org.getId());
        if (merchantIds == null || merchantIds.isEmpty()) {
            return 0;
        }

        Collection<Long> theIds = merchantIds.stream().map(i -> i.longValue()).collect(Collectors.toSet());

//        long merchantId = merchantIds.iterator().next().longValue();

        TypedQuery<Long> tq = entityManager.createQuery("SELECT COUNT(t) FROM DebitTransaction t WHERE t.merchant.merchantId  IN ?1", Long.class);
        tq.setParameter(1, theIds);

        return tq.getSingleResult();
    }

    private long countForAggregator(IOrganization org) {
        Collection<Number> merchantIds = orgRepo.findMerchantIdsByOrganization(org.getId());
        if (merchantIds == null || merchantIds.isEmpty()) {
            return 0;
        }

        Collection<Long> theIds = merchantIds.stream().map(i -> i.longValue()).collect(Collectors.toSet());

        TypedQuery<Long> tq = entityManager.createQuery("SELECT COUNT(t) FROM DebitTransaction t WHERE t.merchant.merchantId IN ?1", Long.class);
        tq.setParameter(1, theIds);

        return tq.getSingleResult();
    }

    private long countForBank(IOrganization org) {
        String bankCode = bankRepository.findCbnCodeByBank(org.getId());
        TypedQuery<Long> tq = entityManager.createQuery("SELECT COUNT(t) FROM DebitTransaction t WHERE t.institution.bankCode = ?1", Long.class);
        tq.setParameter(1, bankCode);

        return tq.getSingleResult();
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

    @Override
    public List<DebitTransaction> getBacklogTransactions() {
        String query = "SELECT d FROM DebitTransaction d WHERE d.responseCode='00' AND  d.creditTransaction.responseCode='00'"
                + " AND d.billed <> true AND d.transactionDate <= ?1 AND (d.fee  > 0 OR d.creditTransaction.fee > 0)";

        TypedQuery<DebitTransaction> tq = entityManager.createQuery(query, DebitTransaction.class);
        LocalDateTime date = LocalDateTime.of(LocalDate.now().minusDays(10), LocalTime.MIN);

        Date startDate = Timestamp.valueOf(date);

        tq.setParameter(1, startDate, TemporalType.TIMESTAMP);
        return tq.getResultList();
    }
}

package com.nibss.tqs.ussd.repositories;

import com.nibss.tqs.ajax.AjaxUssdTransaction;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.queries.QueryDTO;
import com.nibss.tqs.ussd.dto.UssdTransaction;
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
import java.util.*;

/**
 * Created by eoriarewo on 8/30/2016.
 */
public class UssdTransactionRepositoryImpl implements UssdTransactionCustomRepo {

    private EntityManager entityManager;

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private BankRepository bankRepo;

    @Autowired
    public UssdTransactionRepositoryImpl(final JpaContext jpaContext) {
        entityManager = jpaContext.getEntityManagerByManagedType(UssdTransaction.class);
    }

    @Override
    public List<UssdTransaction> getTransactionsForWeeklyBilling() {

        String query = "SELECT t FROM UssdTransaction t WHERE t.sourceResponseCode='00' AND t.destinationResponseCode='00' AND t.responseDate " +
                " BETWEEN :startDate AND :endDate AND t.billed <> true AND t.transactionFee > 0 "
                + " AND t.ussdBiller.id IN (SELECT x.ussdBiller.id FROM UssdFeeSharingConfig x) AND t.telco IS NOT NULL";

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        Date startDate = Timestamp.valueOf(startDateTime);

        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);
        Date endDate = Timestamp.valueOf(endDateTime);

        return getTransactions(startDate, endDate, query);
    }

    @Override
    public List<UssdTransaction> getTransactionsForPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        Date startDate = Timestamp.valueOf(startDateTime);
        Date endDate = Timestamp.valueOf(endDateTime);

        String query = "SELECT t FROM UssdTransaction t WHERE t.sourceResponseCode='00' AND t.destinationResponseCode='00' AND t.responseDate " +
                " BETWEEN :startDate AND :endDate AND t.transactionFee > 0 "
                + " AND t.ussdBiller.id IN (SELECT x.ussdBiller.id FROM UssdFeeSharingConfig x) AND t.telco IS NOT NULL";

        return getTransactions(startDate, endDate, query);
    }

    @Override
    public long filteredCount(QueryDTO queryDTO) {
        TypedQuery<Long> tq = entityManager.createQuery(queryDTO.getQuery(), Long.class);
        applyParams(tq, queryDTO.getParameters());

        return tq.getSingleResult();
    }

    @Override
    public long totalCount(User user) {
        IOrganization org = user.getOrganizationInterface();
        switch (org.getOrganizationType()) {
            case OrganizationType.AGGREGATOR_INT:
                return countForAggregator(org);
            case OrganizationType.BANK_INT:
                return countForBank(org);
            case OrganizationType.MERCHANT_INT:
                return countForMerchaant(org);
            case OrganizationType.NIBSS_INT:
                return countForNibss();
            default:
                return 0;
        }

    }

    @Override
    public List<AjaxUssdTransaction> findTransactions(QueryDTO queryDTO, int start, int end) {
        TypedQuery<AjaxUssdTransaction> tq = entityManager.createQuery(queryDTO.getQuery(), AjaxUssdTransaction.class);
        applyParams(tq, queryDTO.getParameters());
        return tq.getResultList();
    }


    private List<UssdTransaction> getTransactions(final Date startDate, final Date endDate, final String query) {
        TypedQuery<UssdTransaction> tQ = entityManager.createQuery(query, UssdTransaction.class);
        return tQ.setParameter("startDate", startDate, TemporalType.TIMESTAMP)
                .setParameter("endDate", endDate, TemporalType.TIMESTAMP)
                .getResultList();
    }

    private long countForNibss() {
        TypedQuery<Long> tQ = entityManager.createQuery("SELECT COUNT(t) FROM UssdTransaction t", Long.class);
        return tQ.getSingleResult();
    }

    private long countForMerchaant(IOrganization org) {

        Collection<String> ussdCodes = orgRepo.findUssdBillerCodesForOrganization(org.getId());
        if( null == ussdCodes || ussdCodes.isEmpty())
            return  0;

        TypedQuery<Long> tQ = entityManager.createQuery("SELECT COUNT(t) FROM UssdTransaction t WHERE t.ussdBiller.merchantCode= ?1", Long.class);
        tQ.setParameter(1, ussdCodes.iterator().next());

        return tQ.getSingleResult();
    }

    private long countForAggregator(IOrganization org) {
        Collection<String> ussdCodes = orgRepo.findUssdBillerCodesForOrganization(org.getId());
        if( null == ussdCodes || ussdCodes.isEmpty())
            return  0;

        TypedQuery<Long> tQ = entityManager.createQuery("SELECT COUNT(t) FROM UssdTransaction t WHERE t.ussdBiller.merchantCode IN ?1", Long.class);
        tQ.setParameter(1, ussdCodes);

        return tQ.getSingleResult();
    }

    private long countForBank(IOrganization org) {
        String bankCode = bankRepo.findCbnCodeByBank(org.getId());

        TypedQuery<Long> tQ = entityManager.createQuery("SELECT COUNT(t) FROM UssdTransaction t WHERE t.sourceBankCode=?1", Long.class);
        tQ.setParameter(1, bankCode);

        return tQ.getSingleResult();
    }

    private void applyParams(Query query, Map<String, Object> params) {
        if (params == null || params.isEmpty())
            return;

        params.forEach((k, v) -> {
            if (v instanceof Date)
                query.setParameter(k, (Date) v, TemporalType.TIMESTAMP);
            else
                query.setParameter(k, v);
        });
    }

    @Override
    public List<UssdTransaction> getBacklogTransactions() {
        String query = "SELECT t FROM UssdTransaction t WHERE t.sourceResponseCode='00' AND t.destinationResponseCode='00' AND t.responseDate " +
                " <= :date AND t.billed <> true "
                + " AND t.ussdBiller.id IN (SELECT x.ussdBiller.id FROM UssdFeeSharingConfig x) AND t.telco IS NOT NULL";

        TypedQuery<UssdTransaction> tq = entityManager.createQuery(query, UssdTransaction.class);

        LocalDateTime date = LocalDateTime.of(LocalDate.now().minusDays(10), LocalTime.MIN);
        Date startDate = Timestamp.valueOf(date);

        tq.setParameter("date", startDate, TemporalType.TIMESTAMP);

        return tq.getResultList();
    }
}

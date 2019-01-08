package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ajax.AjaxEbillsPayTransaction;
import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.*;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ebillspay.dto.BillingCycle;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.ebillspay.queries.MerchantEbillsPayQueryBuilder;
import com.nibss.tqs.queries.QueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public class EbillsTransactionRepositoryImpl implements EbillsTransactionCustomRepo {

    private EntityManager em;

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private OrganizationSettingRepository orgSettingRepo;

    @Autowired
    public void setJpaContext(final JpaContext jpaContext) {
        em = jpaContext.getEntityManagerByManagedType(EbillspayTransaction.class);
    }

    @Override
    public long totalCount(User user) {
        IOrganization userOrg = user.getOrganizationInterface();

        long totalCount;
        if( userOrg.getOrganizationType() == OrganizationType.MERCHANT_INT)
            totalCount = countForMerchant(userOrg);
        else if( userOrg.getOrganizationType() == OrganizationType.AGGREGATOR_INT)
            totalCount = countForAggregator(userOrg);
        else if(userOrg.getOrganizationType() == OrganizationType.BANK_INT)
            totalCount = countForBank(userOrg);
        else
            totalCount = countForNibss();

        return  totalCount;
    }

    @Override
    public long filteredCount(final QueryDTO queryDTO) {
        TypedQuery<Long> tQ = em.createQuery(queryDTO.getQuery(), Long.class);
        if( null != queryDTO.getParameters() && !queryDTO.getParameters().isEmpty())
            queryDTO.getParameters().forEach((k,v) -> {

                if( v instanceof Date) {
                    Date temp = (Date)v;
                    tQ.setParameter(k, temp, TemporalType.TIMESTAMP);
                } else
                    tQ.setParameter(k,v);
            });

        return tQ.getSingleResult();
    }

    @Override
    public List<AjaxEbillsPayTransaction> findTransactions(final QueryDTO queryDTO, int start, int itemCount) {
        TypedQuery<AjaxEbillsPayTransaction> tQ = em.createQuery(queryDTO.getQuery(),AjaxEbillsPayTransaction.class);
        if( null != queryDTO.getParameters() && !queryDTO.getParameters().isEmpty())
            queryDTO.getParameters().forEach((k,v) -> {

                if( v instanceof Date) {
                    Date temp = (Date)v;
                    tQ.setParameter(k, temp, TemporalType.TIMESTAMP);
                } else
                    tQ.setParameter(k,v);
            });

        if( start > 0 && itemCount > 0) {
            tQ.setFirstResult(start);
            tQ.setMaxResults(itemCount);
        }

        return tQ.getResultList();
    }

    @Override
    public List<EbillspayTransaction> getWeeklyTransactionTimeTransactions() {
        String query = "SELECT t FROM EbillspayTransaction t  WHERE  " +
                "(t.baseTransaction.dateApproved BETWEEN :startDate and :endDate OR t.baseTransaction.chequeConfirmationDate BETWEEN  :startDate AND :endDate)" +
                " AND t.baseTransaction.transactionFee > 0 AND t.billed <> true " +
                "AND t.baseTransaction.biller.ebillsPayTransactionFee.transactionTimeTaken=true AND " +
                "t.baseTransaction.channelCode <> :channelCode AND (t.baseTransaction.responseCode='00' OR t.baseTransaction.chequeResponse = '00' )" +
                "  AND t.baseTransaction.biller.id IN (SELECT e.biller.id FROM EbillsBillingConfiguration e)";

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MAX);

       return getEbillsTransactions(query, startDateTime, endDateTime,null);

    }

    @Override
    public List<EbillspayTransaction> getTransactionTimeTransactions(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        String query = "SELECT t FROM EbillspayTransaction t  WHERE  " +
                "(t.baseTransaction.dateApproved BETWEEN :startDate and :endDate OR t.baseTransaction.chequeConfirmationDate BETWEEN  :startDate AND :endDate)" +
                " AND t.baseTransaction.transactionFee > 0" +
                "  AND t.baseTransaction.biller.ebillsPayTransactionFee.transactionTimeTaken=true AND t.baseTransaction.channelCode <> :channelCode "
                + " AND (t.baseTransaction.responseCode='00' OR t.baseTransaction.chequeResponse = '00' )"
                 + "  AND t.baseTransaction.biller.id IN (SELECT e.biller.id FROM EbillsBillingConfiguration e) ";

        return getEbillsTransactions(query,startDateTime,endDateTime,null);
    }

    private List getEbillsTransactions(String query, LocalDateTime startDateTime, LocalDateTime endDateTime, BillingCycle billingCycle) {
        TypedQuery<EbillspayTransaction> tq = em.createQuery(query,EbillspayTransaction.class);
        Date startDate = Timestamp.valueOf(startDateTime);
        Date endDate = Timestamp.valueOf(endDateTime);

        tq.setParameter("startDate",startDate,TemporalType.TIMESTAMP);
        tq.setParameter("endDate",endDate,TemporalType.TIMESTAMP);
        tq.setParameter("channelCode",appSettings.ussdChannelCode());
        if( null != billingCycle)
            tq.setParameter("billingCycle",billingCycle);

        return tq.getResultList();
    }

    @Override
    public List<EbillspayTransaction> getCustomBillerTransactions(LocalDateTime startDateTime, LocalDateTime endDateTime, BillingCycle billingCycle) {

        String query = "SELECT t FROM EbillspayTransaction t WHERE  " +
                "(t.baseTransaction.dateApproved BETWEEN :startDate and :endDate OR t.baseTransaction.chequeConfirmationDate BETWEEN  :startDate AND :endDate)" +
                " AND t.baseTransaction.transactionFee > 0" +
                "  AND t.baseTransaction.biller.ebillsPayTransactionFee.transactionTimeTaken=false AND t.baseTransaction.channelCode <> :channelCode"
                + " AND (t.baseTransaction.responseCode='00' OR t.baseTransaction.chequeResponse = '00' ) " +
                " AND t.baseTransaction.biller.billerSetting.billingCycle=:billingCycle"
                 + "  AND t.baseTransaction.biller.id IN (SELECT e.biller.id FROM EbillsBillingConfiguration e) ";

        return getEbillsTransactions(query, startDateTime, endDateTime,billingCycle);
    }

    private long countForNibss() {
        String query = "SELECT COUNT(t) FROM BaseTransaction t";
        TypedQuery<Long> tQ = em.createQuery(query,Long.class);
        return tQ.getSingleResult();
    }

    private long countForMerchant(IOrganization org) {

        Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(org.getId());

        int billerId = MerchantEbillsPayQueryBuilder.UNREGISTERED;
        if( null != billerIds && !billerIds.isEmpty())
            billerId = billerIds.iterator().next();

        String query = "SELECT COUNT(t) FROM BaseTransaction t WHERE t.biller.id=:billerId";
        Boolean isAllowed = orgSettingRepo.findShowDateInitiatedByOrginzation(org.getId());
        if( !isAllowed)
            query += " AND (t.dateApproved IS NOT NULL OR t.chequeConfirmationDate IS NOT NULL )";

        TypedQuery<Long> tQ = em.createQuery(query,Long.class);
        tQ.setParameter("billerId",billerId);
        return  tQ.getSingleResult();
    }

    private long countForAggregator(final IOrganization org) {
        Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(org.getId());
        if( null == billerIds || billerIds.isEmpty())
            billerIds = new HashSet<>(Arrays.asList(MerchantEbillsPayQueryBuilder.UNREGISTERED));
        String query = "SELECT COUNT(t) FROM BaseTransaction t WHERE t.biller.id IN :billerIds";

        Boolean isAllowed = orgSettingRepo.findShowDateInitiatedByOrginzation(org.getId());
        if( !isAllowed)
            query += " AND (t.dateApproved IS NOT NULL OR t.chequeConfirmationDate IS NOT NULL )";

        TypedQuery<Long> tQ = em.createQuery(query,Long.class);
        tQ.setParameter("billerIds",billerIds);
        return tQ.getSingleResult();
    }

    private long countForBank(IOrganization org) {

        String query = "SELECT COUNT(t) FROM BaseTransaction t WHERE t.sourceBank.code = :code";
        Boolean isAllowed = orgSettingRepo.findShowDateInitiatedByOrginzation(org.getId());
        if( !isAllowed)
            query += " AND (t.dateApproved IS NOT NULL OR t.chequeConfirmationDate IS NOT NULL )";

        TypedQuery<Long> tQ = em.createQuery(query,Long.class);
        tQ.setParameter("code",bankRepository.findCbnCodeByBank(org.getId()));
        return tQ.getSingleResult();
    }

    @Override
    public List<EbillspayTransaction> getBacklogsForTransactionTimeTaken() {
         String query = "SELECT t FROM EbillspayTransaction t WHERE  (t.baseTransaction.dateApproved <= :date OR t.baseTransaction.chequeConfirmationDate <= :date) AND t.baseTransaction.transactionFee > 0" +
                "  AND t.baseTransaction.biller.ebillsPayTransactionFee.transactionTimeTaken=true AND t.baseTransaction.channelCode <> :channelCode AND t.billed <> true "
                 + " AND (t.baseTransaction.responseCode='00' OR t.baseTransaction.chequeResponse = '00' ) "
                 + "  AND t.baseTransaction.biller.id IN (SELECT e.biller.id FROM EbillsBillingConfiguration e) ";
         
         TypedQuery<EbillspayTransaction> tq = em.createQuery(query,EbillspayTransaction.class);
         tq.setParameter("channelCode", appSettings.ussdChannelCode());
         
         LocalDateTime date = LocalDateTime.of(LocalDate.now().minusDays(10), LocalTime.MIN);
         Date startDate = Timestamp.valueOf(date);
         
         tq.setParameter("date", startDate,TemporalType.TIMESTAMP);
         return tq.getResultList();
    }

    @Override
    public List<EbillspayTransaction> getBacklogsForNonTransactionTimeTaken() {
        String query = "SELECT t FROM EbillspayTransaction t WHERE (t.baseTransaction.dateApproved <= :date OR t.baseTransaction.chequeConfirmationDate <= :date) AND t.baseTransaction.transactionFee > 0" +
                "  AND t.baseTransaction.biller.ebillsPayTransactionFee.transactionTimeTaken=false AND t.baseTransaction.channelCode <> :channelCode AND t.billed <> true "
                + " AND (t.baseTransaction.responseCode='00' OR t.baseTransaction.chequeResponse = '00' ) "
                 + "  AND t.baseTransaction.biller.id IN (SELECT e.biller.id FROM EbillsBillingConfiguration e) ";
         
         TypedQuery<EbillspayTransaction> tq = em.createQuery(query,EbillspayTransaction.class);
         tq.setParameter("channelCode", appSettings.ussdChannelCode());
         
         LocalDateTime date = LocalDateTime.of(LocalDate.now().minusDays(10), LocalTime.MIN);
         Date startDate = Timestamp.valueOf(date);
         
         tq.setParameter("date", startDate,TemporalType.TIMESTAMP);
         return tq.getResultList();
    }
}

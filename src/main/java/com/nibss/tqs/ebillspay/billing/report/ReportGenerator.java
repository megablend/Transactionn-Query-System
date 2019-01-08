package com.nibss.tqs.ebillspay.billing.report;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.Aggregator;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.repositories.AggregatorRepository;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.ebillspay.dto.Bank;
import com.nibss.tqs.ebillspay.dto.EbillspayTransaction;
import com.nibss.tqs.ebillspay.dto.SharedTransaction;
import com.nibss.tqs.ebillspay.repositories.BankRepository;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Emor on 7/28/2016.
 *
 * this class is meant to help with generating CSV and PDF reports
 * for both banks and aggregators.
 * Prior to this upgrade, separate reports were generated for banks
 * that served biller bank, aggregator and collecting bank, even if they were the same entity.
 * this application will seek to address that.
 */
public class ReportGenerator implements Serializable, Runnable{

    private BankRepository bankRepository;
    private ApplicationSettings appSettings;

    private OrganizationRepository organizationRepository;

    @Getter @Setter
    private List<SharedTransaction> billedTransactions;


    private AggregatorRepository aggregatorRepository;


    @Override
    public void run() {
        if( billedTransactions == null || billedTransactions.isEmpty())
            return;

        //group the transactions by collecting bank.
        //for each transaction, get the aggregator and the biller bank share
        //if these fields are not null, find out if each

        List<Bank> allBanks = bankRepository.findAll();

        Map<Bank,Map<SharedTransaction,BigDecimal>> bankMap = new HashMap<>();

        //split first by bank
        for(Bank bank : allBanks) {
            bankMap.put(bank,new HashMap<>());
        }

        for(SharedTransaction t : billedTransactions) {
            for(Bank bank : bankMap.keySet()) {
                computeSumForCollectingBank(bankMap.get(bank),t,bank);
            }
        }

        //split by non-bank aggregator
        List<Aggregator> allAggregators = aggregatorRepository.findAll();
        Map<Aggregator,Map<SharedTransaction,BigDecimal>> aggregatorMap = new HashMap<>();

        allAggregators.forEach( a -> aggregatorMap.put(a, new HashMap<>()));

        for(SharedTransaction t : billedTransactions) {
            for(Aggregator a : aggregatorMap.keySet()) {
                computeForNonBankAggregator(aggregatorMap.get(a),t,a);

            }
        }

        //generate CSV and PDF reports for both banks and non-banks

    }

    private void computeSumForCollectingBank(final Map<SharedTransaction,BigDecimal> amountMap, SharedTransaction t,Bank bank) {
        BigDecimal totalShare = BigDecimal.ZERO;
        if(t.getCollectingBankShare() != null) {

           if( t.getTransaction().getBaseTransaction().getSourceBank().getCode().equals(bank.getCode())) {
               totalShare = totalShare.add(t.getCollectingBankShare());
               t.setCollectingBankShare(null);
           }

        }

        if(t.getBillerBankShare() != null) {
            //check if this bank is also the collecting bank
            if( bank.getCode().equals(t.getBillingConfiguration().getBillerBankCode())) {
                totalShare = totalShare.add(t.getBillerBankShare());
                t.setBillerBankShare(null);
            }
        }
        if( t.getAggregatorShare() != null) {
            //check if aggregator is same as bank
            List<Organization> orgs = organizationRepository.findAggregatorForEbillsPayBiller(t.getTransaction().getBaseTransaction().getBiller().getId());
            if( null != orgs && !orgs.isEmpty()) {
                Organization org = orgs.get(0);
                if( org != null) {
                    if( org instanceof com.nibss.tqs.core.entities.Bank) {
                        String bankCode = ((com.nibss.tqs.core.entities.Bank)org).getCbnBankCode();
                        if( bankCode.equals(bank.getCode())) {
                            totalShare = totalShare.add(t.getAggregatorShare());
                            t.setAggregatorShare(null);
                        }
                    }
                }
            }
        }
        amountMap.put(t,totalShare);
    }

    private  void computeForNonBankAggregator(final Map<SharedTransaction,BigDecimal> amountMap, SharedTransaction t, Aggregator aggregator) {
        BigDecimal totalshare = BigDecimal.ZERO;
        if( t.getAggregatorShare() != null) {
            List<Organization> orgs = organizationRepository.findAggregatorForEbillsPayBiller(t.getTransaction().getBaseTransaction().getBiller().getId());

            if( null != orgs && !orgs.isEmpty()) {
                Organization org = orgs.get(0);
                if( org != null) {
                    if( org instanceof Aggregator) {
                        String code = ((Aggregator)org).getCode();
                        if( code.equals(aggregator.getCode())) {
                            totalshare = totalshare.add(t.getAggregatorShare());
                            t.setAggregatorShare(null);
                        }
                    }
                }
            }
        }

        amountMap.put(t,totalshare);

    }
}

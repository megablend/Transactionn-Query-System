package com.nibss.tqs.ussd.billing;

import com.nibss.tqs.core.entities.BankAccount;
import com.nibss.tqs.ussd.dto.UssdBiller;
import com.nibss.tqs.ussd.dto.UssdFeeSharingConfig;
import com.nibss.tqs.ussd.dto.UssdTransaction;
import org.apache.commons.csv.CSVFormat;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 8/15/2016.
 */
@Component
public class UssdBilling {

    public void doBilling() {
          /*
        1. get transactions yet to be billed for period
        2. group txns by collecting bank
        3. sum transaction fee to be debited
        4. generate smartdet based on 3
        5. share transaction fee for each of the parties involved
        6. generate payment files/send to hawk?
         */

        List<UssdTransaction> transactions = new ArrayList<>();
        //group trxns by src bank code

        Map<String, List<UssdTransaction>> trxnsBySourceBank = transactions.stream().collect(Collectors.groupingBy( t -> t.getSourceBankCode()));

        Map<String,BigDecimal> smartDetMap = new HashMap<>();
        trxnsBySourceBank.forEach( (k,v) -> smartDetMap.put(k, v.stream().map(t -> t.getTransactionFee()).reduce((a,b) -> a.add(b)).get()));

        CSVFormat smartDetFmt = CSVFormat.DEFAULT;
        //TODO: generate smartdet file here

        //group trxns by ussd biller
        Map<UssdBiller,List<UssdTransaction>> trxnsByBiller = transactions.stream().collect(Collectors.groupingBy( t-> t.getUssdBiller()));

        //map of bank account to their amount due
        Map<BankAccount,BigDecimal> partiesMap = new HashMap<>();

        List<UssdTransaction> billedTransactions  = new ArrayList<>();

        //for each biller, get the config and apply rules to their transactions.
        for( UssdBiller biller : trxnsByBiller.keySet()) {
            UssdFeeSharingConfig config = biller.getFeeSharingConfig();
            if( null == config)
                continue;

            List<UssdTransaction> billerTrxns = trxnsByBiller.get(biller);
            for( UssdTransaction t : billerTrxns) {
                computePartyFees(biller,t,config, partiesMap);
                billedTransactions.add(t);
            }

        }
    }

    private void computePartyFees(UssdBiller biller,UssdTransaction t, UssdFeeSharingConfig config, Map<BankAccount,BigDecimal> partyMap) {
        BigDecimal aggregatorShare = config.getAggregatorShare();
        BigDecimal collectingBankShare = config.getCollectingBankShare();
        BigDecimal billerBankShare = config.getBillerBankShare();
        BigDecimal ussdAggregatorShare = config.getUssdAggregatorShare();
        BigDecimal telcoShare = config.getTelcoShare();
        //BigDecimal nibssShare = BigDecimal.ZERO;

        if( config.isPercentage()) {
            aggregatorShare = t.getTransactionFee().multiply(config.getAggregatorShare());
            collectingBankShare = t.getTransactionFee().multiply(config.getCollectingBankShare());
            billerBankShare = t.getTransactionFee().multiply(config.getBillerBankShare());
            ussdAggregatorShare = t.getTransactionFee().multiply(config.getUssdAggregatorShare());
            telcoShare = t.getTransactionFee().multiply(config.getTelcoShare());
        }

        //TODO: Henry says he'll start including telco info per transaction. Follow up on him

        
    }
}

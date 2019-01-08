package com.nibss.tqs.corporatelounge.billing;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountBalance;
import com.nibss.corporatelounge.dto.Bank;
import com.nibss.corporatelounge.dto.BillingSetting;
import com.nibss.tqs.billing.BillingProvider;
import com.nibss.tqs.core.entities.Product;
import com.nibss.tqs.report.BaseReportHelper;
import com.nibss.tqs.report.BillingReportGenerator;
import com.nibss.tqs.report.TransactionReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by eoriarewo on 10/23/2017.
 */
@Component
public class CorporateLoungeBillingReportHelper extends BaseReportHelper {

    @Autowired
    private BillingReportGenerator billingReportGenerator;

    public void generateAnnualBillingReport(List<Account> accounts, BillingSetting setting) throws Exception {
        Map<Bank, List<Account>> byBank = accounts.stream().collect(groupingBy(a -> a.getBank()));

        Map<String, List<TransactionReport>> map = new HashMap<>();
        byBank.forEach((k, v) -> {
            List<TransactionReport> lst = new ArrayList<>(v.size());
            v.forEach(a -> {
                TransactionReport report = new TransactionReport(a);
                report.setFee(setting.getAnnualFee());
                report.setCalculatedAmount(setting.getAnnualFee().multiply(setting.getBankShare())
                        .setScale(2, BillingProvider.ROUNDING_MODE));
                lst.add(report);
            });

            map.put(k.getCbnCode(), lst);
        });

        List<TransactionReport> nibss = new ArrayList<>(accounts.size());
        accounts.forEach(a -> {
            TransactionReport temp = new TransactionReport(a);
            temp.setFee(setting.getAnnualFee());
            temp.setCalculatedAmount(setting.getAnnualFee().multiply(setting.getNibssShare()).setScale(2, BillingProvider.ROUNDING_MODE));
            nibss.add(temp);
        });

        map.put(NIBSS_CODE, nibss);

        billingReportGenerator.generateReports(map, Product.CLOUNGE_ANNUAL);
    }


    public void generatePerTransactionBillingReport(List<AccountBalance> balances, BillingSetting setting) throws Exception {
        Map<Bank, List<AccountBalance>> byBank = balances.stream().collect(groupingBy(a -> a.getAccount().getBank()));


        Map<String, List<TransactionReport>> map = new HashMap<>();
        byBank.forEach((k, v) -> {
            List<TransactionReport> lst = new ArrayList<>(v.size());
            v.forEach(a -> {
                TransactionReport report = new TransactionReport(a);
                report.setFee(setting.getPerTransactionFee());
                report.setCalculatedAmount(setting.getPerTransactionFee().multiply(setting.getBankShare()).setScale(2, BillingProvider.ROUNDING_MODE));
                lst.add(report);
            });

            map.put(k.getCbnCode(), lst);
        });

        List<TransactionReport> nibss = new ArrayList<>(balances.size());
        balances.forEach(a -> {
            TransactionReport temp = new TransactionReport(a);
            temp.setFee(setting.getPerTransactionFee());
            temp.setCalculatedAmount(setting.getPerTransactionFee().multiply(setting.getNibssShare()).setScale(2, BillingProvider.ROUNDING_MODE));
            nibss.add(temp);
        });

        map.put(NIBSS_CODE, nibss);

        billingReportGenerator.generateReports(map, Product.CLOUNGE_PER_TRANSACTION);
    }
}

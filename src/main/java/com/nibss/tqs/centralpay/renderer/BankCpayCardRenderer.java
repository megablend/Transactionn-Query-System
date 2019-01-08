package com.nibss.tqs.centralpay.renderer;

import com.nibss.tqs.ajax.AjaxCpayCardTransaction;
import com.nibss.tqs.centralpay.dto.CardTransaction;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.report.BankTransactionReportDownloader;
import com.nibss.tqs.report.DownloadType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by Emor on 8/1/2016.
 */
@Component
@Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BankCpayCardRenderer implements CpayCardRenderer {

    @Autowired
    private BankTransactionReportDownloader reportDownloader;

    @Override
    public List<String> getTableHeader() {
        return null;
    }

    @Override
    @Transactional
    public JQueryDataTableResponse<Object> render(List<AjaxCpayCardTransaction> transactions, JQueryDataTableRequest request, User user) {
        return null;
    }

    @Override
    @Transactional
    public ByteArrayOutputStream download(DownloadType downloadType, List<AjaxCpayCardTransaction> transactions) {
        try {
            return reportDownloader.generateCpayCardReport(transactions,downloadType);
        } catch (Exception e) {
            throw new RuntimeException("cld not gen. card trxn rpt",e);
        }
    }


}

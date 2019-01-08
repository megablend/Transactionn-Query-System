package com.nibss.tqs.renderer;

import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;

import java.io.Serializable;
import java.util.List;

/**
 * Created by eoriarewo on 9/1/2016.
 */
public interface TransactionRenderer {

    String SUCCCESS_STATUS = "00";
    String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    String NUMBER_FORMAT_PATTERN = "#,###,###,##0.00";

    List<String> getTableHeader();
}

package com.nibss.tqs.report;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Emor on 9/27/16.
 */
@Data
public class BillingFile implements Serializable {

    private String fileName;
    private Date dateCreated;
    private String fileType;
}

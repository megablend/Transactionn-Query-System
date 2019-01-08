package com.nibss.tqs.ajax;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Emor on 7/9/16.
 */
@Data
public class AjaxResponse implements Serializable {


    public static final String FAILED = "failed";

    public  static  final String SUCCESS = "success";

    public AjaxResponse() {
        errors = new ArrayList<>();
        extras = new HashMap<>();
    }

    private String message;

    private String status;

    private List<String> errors;

    private Map<String,Objects> extras;
}

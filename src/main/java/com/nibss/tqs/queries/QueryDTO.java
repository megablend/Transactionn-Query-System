package com.nibss.tqs.queries;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Data
public class QueryDTO implements Serializable {

    public QueryDTO() {
        parameters = new HashMap<>();
    }

    private String query;
    private Map<String,Object> parameters;

    Integer startIndex;

    Integer pageSize;
}

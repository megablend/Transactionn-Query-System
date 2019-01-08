package com.nibss.tqs.ajax;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by eoriarewo on 7/8/2016.
 */
@Data
@NoArgsConstructor
public class SearchDTO implements Serializable {

    public SearchDTO(Object id, String text) {
        this.id = id.toString();
        this.text = text;
    }
    private String id;
    private String text;
}

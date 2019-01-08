package com.nibss.tqs.ajax;

import com.nibss.tqs.core.repositories.IProduct;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by eoriarewo on 6/30/2017.
 */
@Data
public class AjaxProduct implements Serializable, IProduct {

    private String name;

    private int id;

    private String code;


    public AjaxProduct() {

    }

    public AjaxProduct(int id, String name) {
        this.id = id;
        this.name = name;

    }

    public AjaxProduct(int id, String name, String code) {
        this(id,name);
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}

package com.nibss.tqs.ajax;

import com.nibss.tqs.core.repositories.IOrganization;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eoriarewo on 6/30/2017.
 */
@Data
@NoArgsConstructor
public class AjaxOrganization implements Serializable, IOrganization {

    private int id;

    private String name;
    private int type;

    @Setter
    @Getter
    private List<String> productCodes = new ArrayList<>(0);

    public  AjaxOrganization(int id, String name, int type) {
        this.id = id;
        this.name = name;
        this.type = type;

    }

    public  AjaxOrganization(int id, String name, int type, List<String> productCodes) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.productCodes = productCodes;

    }

    @Override
    public int getOrganizationType() {
        return type;
    }

    @Override
    public List<String> getProductCodes() {
        return productCodes;
    }

    @Override
    public String toString() {
        return name;
    }
}

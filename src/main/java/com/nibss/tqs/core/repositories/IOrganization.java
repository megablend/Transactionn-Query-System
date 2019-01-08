package com.nibss.tqs.core.repositories;

import java.util.List;

/**
 * Created by eoriarewo on 6/30/2017.
 */
public interface IOrganization {
    int getId();
    String getName();
    int getOrganizationType();

    List<String> getProductCodes();
}

package com.nibss.tqs.ebillspay.repositories;

import java.util.List;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public interface UserParamCustomRepo {

    List<String> getParamNamesForBiller( int billerId);
}

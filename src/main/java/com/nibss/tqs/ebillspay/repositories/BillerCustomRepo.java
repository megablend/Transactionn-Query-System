package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ebillspay.dto.Biller;

import java.util.List;

/**
 * Created by eoriarewo on 7/11/2016.
 */
public interface BillerCustomRepo {
    List<Biller> findByIds( List<Integer> ids);
}

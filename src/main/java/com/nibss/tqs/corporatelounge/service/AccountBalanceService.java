package com.nibss.tqs.corporatelounge.service;

import com.nibss.tqs.corporatelounge.ajax.AccountBalanceDto;
import com.nibss.tqs.corporatelounge.repositories.AccountBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by eoriarewo on 10/4/2017.
 */
@Service
public class AccountBalanceService {

    @Autowired
    private AccountBalanceRepository repo;



}

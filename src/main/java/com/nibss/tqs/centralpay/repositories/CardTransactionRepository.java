package com.nibss.tqs.centralpay.repositories;

import com.nibss.tqs.centralpay.dto.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by eoriarewo on 7/4/2016.
 */
public interface CardTransactionRepository extends JpaRepository<CardTransaction,String>, CardTransactionCustomRepo{

}

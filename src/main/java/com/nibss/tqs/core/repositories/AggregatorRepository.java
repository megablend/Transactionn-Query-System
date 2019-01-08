package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Aggregator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by eoriarewo on 7/21/2016.
 */
public interface AggregatorRepository extends JpaRepository<Aggregator,Integer> {

    Aggregator findByCode(String code);

    int countByCode(String code);

    @Query("SELECT a.code FROM Aggregator a WHERE a.id = ?1")
    String findCodeForAggregator(int id);
}

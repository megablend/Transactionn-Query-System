package com.nibss.tqs.ussd.repositories;

import com.nibss.tqs.ussd.dto.UssdFeeSharingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by eoriarewo on 8/22/2016.
 */
public interface UssdFeeSharingRepository extends JpaRepository<UssdFeeSharingConfig,Integer> {
}

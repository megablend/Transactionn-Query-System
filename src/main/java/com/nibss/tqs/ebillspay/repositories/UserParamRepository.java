package com.nibss.tqs.ebillspay.repositories;

import com.nibss.tqs.ebillspay.dto.UserParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Transactional
public interface UserParamRepository extends JpaRepository<UserParam,Long>, UserParamCustomRepo {
    List<UserParam> findBySessionId(String sessionId);
 }

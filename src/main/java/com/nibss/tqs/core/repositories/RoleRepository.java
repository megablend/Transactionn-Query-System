package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Role;
import org.apache.xalan.xsltc.util.IntegerArray;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Emor on 7/9/16.
 */
public interface RoleRepository extends JpaRepository<Role,Integer> {

    Role findByName(String name);
}

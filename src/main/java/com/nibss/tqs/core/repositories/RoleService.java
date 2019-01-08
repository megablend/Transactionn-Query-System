package com.nibss.tqs.core.repositories;

import com.nibss.tqs.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by eoriarewo on 6/14/2017.
 */

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Cacheable(cacheNames = "roles", unless = "#result == null")
    public Role findByName(String roleName) {
        return  roleRepository.findByName(roleName);
    }

    @Cacheable(cacheNames = "roles", unless = "#result == null || #result.empty")
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public void save(List<Role> roles) {
        roleRepository.save(roles);
    }

    public void save(Role role) {
        roleRepository.save(role);
    }
}

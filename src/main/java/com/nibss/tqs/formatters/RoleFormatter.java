package com.nibss.tqs.formatters;

import com.nibss.tqs.core.entities.Role;
import com.nibss.tqs.core.repositories.RoleRepository;
import com.nibss.tqs.core.repositories.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by eoriarewo on 8/1/2016.
 */
@Component
@Slf4j
public class RoleFormatter implements Formatter<Role> {

    private final String ROLE_PREFIX = "ROLE_";
    @Autowired
    private RoleService roleService;

    @Override
    public Role parse(String s, Locale locale) throws ParseException {

        return  roleService.findByName(s);

//        String roleName = s.toUpperCase().replace(" ","_");
//        if(!roleName.startsWith(ROLE_PREFIX))
//            roleName = ROLE_PREFIX + roleName;
//
//        try {
//            return roleRepository.findByName(roleName);
//        } catch (Exception e) {
//            log.error("could not fetch role",e);
//            return null;
//        }
    }

    @Override
    public String print(Role role, Locale locale) {
//        return role.getName().replace(ROLE_PREFIX,"");
        return role.getName();
    }
}

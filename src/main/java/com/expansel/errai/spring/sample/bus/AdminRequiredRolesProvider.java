package com.expansel.errai.spring.sample.bus;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;

/**
 * Illustrates that RequiredRolesProvider works. Although the default SpringAuthenticationService only works
 * with RoleImpl classes. To use other implementations, subclass the SpringAuthenticationService to provide 
 * a different Spring GrantedAuthority <-> Errai Role mapping.
 *
 * @author Zach Visagie
 */
public class AdminRequiredRolesProvider implements RequiredRolesProvider {

    @Override
    public Set<Role> getRoles() { 
        return Stream.of(new RoleImpl("admin")).collect(Collectors.toSet());
    }

}

package com.expansel.errai.spring.sample.bus;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.User.StandardUserProperties;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.keycloak.adapters.springsecurity.authentication.KeycloakLogoutHandler;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.expansel.errai.springsecurity.server.SpringSecurityAuthenticationService;

/**
 * This is just a sample implementation for testing purposes as there are many more properties one can add 
 * to the Errai User as well as adding groups etc.
 *
 * @author Zach Visagie
 */
@Service
public class KeycloakSpringAuthenticationService extends SpringSecurityAuthenticationService implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakSpringAuthenticationService.class);

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected KeycloakLogoutHandler keycloakLogoutHandler;
    
    public KeycloakSpringAuthenticationService(AuthenticationManager authenticationManager, HttpSession session, HttpServletRequest request, HttpServletResponse response, KeycloakLogoutHandler keycloakLogoutHandler) {
        super(authenticationManager, session);
        this.request = request;
        this.response = response;
        this.keycloakLogoutHandler = keycloakLogoutHandler;
    }

    @Override
    public void logout() {
        logger.info("Logging out with KeycloakLogoutHandler");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            keycloakLogoutHandler.logout(request, response, authentication);
        }
        super.logout();
    }
    
    @Override
    protected User userFromAuthentication(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        Collection<? extends Role> erraiRoles = authoritiesToErraiRoles(authorities);
        // auth.name comes back with UUID, so override super class and get username from keycloak token
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) auth;
        AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();
        String name = accessToken.getPreferredUsername();
        String email = accessToken.getEmail();
        String firstName = accessToken.getGivenName();
        String lastName = accessToken.getFamilyName();
        
        User user = new UserImpl(name, erraiRoles);
        user.setProperty(StandardUserProperties.EMAIL, email);
        user.setProperty(StandardUserProperties.FIRST_NAME, firstName);
        user.setProperty(StandardUserProperties.LAST_NAME, lastName);
        return user;
    }
}

package com.expansel.errai.spring.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.core.GrantedAuthorityDefaults;

/**
 * <p>This config could have been applied on WebSecurityConfig and WebConfig via annotations 
 * but to get @secured working the RoleVoter's prefix had to be changed. There is an 
 * outstanding bug on Spring security for resolving this.</p>  
 *
 * <p>The other advantage is that we can not repeat ourselves if we use the same config for 
 * each context, like the root context and the web context</p>
 * 
 * @author Zach Visagie
 */
@Configuration
@EnableGlobalMethodSecurity(
        prePostEnabled = true, 
        securedEnabled = true, 
        jsr250Enabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    /**
     * <p>Avoid Spring security applied ROLE_ prefix to authorities. This is something 
     * used to distinguish roles from other types of authorities in voters and 
     * therefore not recommended by spring docs, but not using it since we are only 
     * using roles and the ROLE_ prefix concept rather obscure for many users.</p>
     * 
     * <p>See this reference: https://stackoverflow.com/questions/11539162/why-does-spring-securitys-rolevoter-need-a-prefix</p>
     */
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
    }

    /**
     * GrantedAuthorityDefaults not applied when creating the accessDecisionManager() as it uses 
     * new RoleVoter unlike web security config which uses RoleVoterBeanFactory
     */
    @Override
    protected AccessDecisionManager accessDecisionManager() {
        AffirmativeBased accessDecisionManager = (AffirmativeBased) super.accessDecisionManager();

        //Remove the ROLE_ prefix from RoleVoter for @Secured and hasRole checks on methods
        accessDecisionManager.getDecisionVoters().stream()
                .filter(RoleVoter.class::isInstance)
                .map(RoleVoter.class::cast)
                .forEach(it -> it.setRolePrefix(""));

        return accessDecisionManager;
    }
}
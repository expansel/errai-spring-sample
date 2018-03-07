package com.expansel.errai.spring.sample.config;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakLogoutHandler;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import com.expansel.errai.erraisecurity.server.RestrictedAccessAspect;
import com.expansel.errai.erraisecurity.server.SpringRequiredRolesExtractor;
import com.expansel.errai.spring.sample.bus.AdminRequiredRolesProvider;
import com.expansel.errai.spring.sample.bus.KeycloakSpringAuthenticationService;
import com.expansel.errai.springsecurity.server.ErraiClientBusAuthenticationEntryPoint;
import com.expansel.errai.springsecurity.server.ErraiRestClientAuthenticationEntryPoint;

/**
 * <p>Spring security config for keycloak, imported into root application context {@link AppConfig}</p>
 * 
 * Can be enabled with Spring's Profile mechanism by activating the 'keycloak' profile either through web.xml or 
 * a system property.
 *
 * @author Zach Visagie
 */
@Profile("keycloak")
@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
public class KeycloakWebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakWebSecurityConfig.class);

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    /**
     * Defines the session authentication strategy.
     */
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        // Note we set up two separate authentication entry point classes, although we could have used
        // a single one and done our decision making in the commence method, but this way concerns are 
        // better separated and use spring infrastructure for url matching.
        
        // match requests coming from the errai message bus as configured on Errai servlet in web.xml 
        AntPathRequestMatcher clientBusMatcher = new AntPathRequestMatcher("/**/*.erraiBus");
        
        // match rest requests
        // if the rest service also serves external parties then another auth mechanism
        // needs to be set up eg. basic or oauth. See this for examples:
        // http://www.baeldung.com/spring-security-multiple-entry-points
        AntPathRequestMatcher restClientMatcher = new AntPathRequestMatcher("/rest/**");
        
        // Since we are illustrating a mix of bus and client side jaxrs requests our errai client 
        // matches either bus or rest requests.
        OrRequestMatcher erraiClientMatcher = new OrRequestMatcher(Arrays.asList(clientBusMatcher, restClientMatcher));

        // match requests not coming from client bus or client jaxrs which should be redirected to html login page
        // and not return json
        NegatedRequestMatcher notErraiBusMatcher = new NegatedRequestMatcher(erraiClientMatcher);

        // disabling CSRF as it messes with Errai MessageBus, still need to
        // figure out how to fix it
        http.requestCache()
             // disabling request cache because ajax requests such as message bus should not be saved and used for 
             // login success redirection, but this is exactly what happens with keycloak in my current setup
             // after login I get redirect to something like this:
             //     http://localhost:8080/errai-spring-sample/out.54846-4558.erraiBus?z=1&clientId=54846-4558
             //  
             // The default RequestCacheConfigurer sets up request matchers that do not match errai bus 
             // requests as they don't have Accept set to application/json and neither do they set the 
             // X-Requested-With header.
             //
             // This can of course be customized, but for our illustration turning it off is fine. 
            .requestCache(new NullRequestCache())
            .and()
            .csrf()
            .disable()
            .anonymous().disable()
            .authorizeRequests()
            .antMatchers("/**").authenticated()
            .and()
            .logout()
            .logoutUrl("/logout").permitAll()
            .logoutSuccessUrl("/index.html")
            .and()
            .exceptionHandling()
            // normal html login entry point
            .defaultAuthenticationEntryPointFor(authenticationEntryPoint(),
                    notErraiBusMatcher)
            // client bus json response to generate security error client side
            .defaultAuthenticationEntryPointFor(new ErraiClientBusAuthenticationEntryPoint(),
                    clientBusMatcher)
            // rest client json response to generate security error client side
            .defaultAuthenticationEntryPointFor(new ErraiRestClientAuthenticationEntryPoint(),
                    restClientMatcher);
    }
    
    /**
     * Exposing to context so we can wire into {@link KeycloakSpringAuthenticationService}
     */
    @Bean
    @Override
    public KeycloakLogoutHandler keycloakLogoutHandler() throws Exception {
        return super.keycloakLogoutHandler();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
    }
        
    @Bean
    public AuthenticationManager exposedAuthenticationManager() throws Exception {
        return authenticationManager();
    }

    @Bean
    public RestrictedAccessAspect restrictedAccessAspect(AuthenticationService authenticationService,
            RequiredRolesExtractor roleExtractor) {
        logger.info("Creating RestrictedAccessAspect");
        return new RestrictedAccessAspect(authenticationService, roleExtractor);
    }

    @Bean
    public SpringRequiredRolesExtractor requiredRolesExtractor(ApplicationContext context) {
        logger.info("Creating SpringRequiredRolesExtractor");
        return new SpringRequiredRolesExtractor(context);
    }

    @Bean
    public KeycloakSpringAuthenticationService authenticationService(AuthenticationManager authenticationManager,
            HttpSession session, HttpServletRequest request, HttpServletResponse response, KeycloakLogoutHandler keycloakLogoutHandler) {
        logger.info("Creating KeycloakSpringAuthenticationService");
        return new KeycloakSpringAuthenticationService(authenticationManager, session, request, response, keycloakLogoutHandler);
    }

    @Bean
    public AdminRequiredRolesProvider adminRequiredRolesProvider() {
        logger.info("Creating AdminRequiredRolesProvider");
        return new AdminRequiredRolesProvider();
    }
}
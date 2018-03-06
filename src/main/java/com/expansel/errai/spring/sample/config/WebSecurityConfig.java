package com.expansel.errai.spring.sample.config;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.context.annotation.SessionScope;

import com.expansel.errai.erraisecurity.server.RestrictedAccessAspect;
import com.expansel.errai.erraisecurity.server.SpringRequiredRolesExtractor;
import com.expansel.errai.spring.sample.bus.AdminRequiredRolesProvider;
import com.expansel.errai.springsecurity.server.ErraiClientBusAuthenticationEntryPoint;
import com.expansel.errai.springsecurity.server.ErraiRestClientAuthenticationEntryPoint;
import com.expansel.errai.springsecurity.server.SpringSecurityAuthenticationService;

/**
 * <p>Spring security config, imported into root application context {@link AppConfig}</p>
 * 
 * <p>Note we currently don;</p>
 *
 * @author Zach Visagie
 */
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(
//        prePostEnabled = true, 
//        securedEnabled = false, 
//        jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
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
        http.csrf()
            .disable()
            .authorizeRequests()
            .antMatchers("/login")
            .permitAll()
            .antMatchers("/**")
            .authenticated()
            .and()
            .formLogin()
            .defaultSuccessUrl("/", true)
            .and()
            .exceptionHandling()
            // normal html login entry point
            .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
                    notErraiBusMatcher)
            // client bus json response to generate security error client side
            .defaultAuthenticationEntryPointFor(new ErraiClientBusAuthenticationEntryPoint(),
                    clientBusMatcher)
            // rest client json response to generate security error client side
            .defaultAuthenticationEntryPointFor(new ErraiRestClientAuthenticationEntryPoint(),
                    restClientMatcher);
        
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
    }
        
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication = auth.inMemoryAuthentication();

        // The {} notation in passwords can point to a hashing algorithm
        // {noop} basically says the value is unhashed
        inMemoryAuthentication.withUser("user")
                              .password("{noop}11")
                              .authorities("user");
        inMemoryAuthentication.withUser("admin")
                              .password("{noop}11")
                              .authorities("admin");
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
    @SessionScope
    public SpringSecurityAuthenticationService authenticationService(AuthenticationManager authenticationManager,
            HttpSession session) {
        // TODO Clarify: I think Errai CDI by default uses session scope for authentication service, but 
        // This implementation could do fine as a singleton as well
        logger.info("Creating SpringSecurityAuthenticationService");
        return new SpringSecurityAuthenticationService(authenticationManager, session);
    }

    @Bean
    public AdminRequiredRolesProvider adminRequiredRolesProvider() {
        logger.info("Creating AdminRequiredRolesProvider");
        return new AdminRequiredRolesProvider();
    }
}
package com.expansel.errai.spring.sample.config;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.expansel.errai.erraisecurity.server.RestrictedAccessAspect;
import com.expansel.errai.spring.sample.controller.GlobalResponseEntityExceptionHandler;
import com.expansel.errai.spring.sample.controller.HelloWorldController;

/**
 * <p>Configured in web.xml on Dispatcher Servlet.</p>
 * 
 * <p>Not enabling {@link EnableAspectJAutoProxy} which allows for the {@link RestrictedAccessAspect} to
 * apply to Spring controllers so you can use {@link RestrictedAccess} on them. It is probably 
 * better to stick to Spring or jsr250 annotations on them. Exceptions handled by 
 * {@link GlobalResponseEntityExceptionHandler}</p>
 *
 * @author Zach Visagie
 */
@Configuration
@EnableWebMvc
@Import({WebSecurityConfig.class, MethodSecurityConfig.class})
//@EnableAspectJAutoProxy // need this for @RestrictedAccess on Controllers, not recommended
public class WebConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    /**
     * This handles security exceptions from controllers globally, writing appropriate 
     * json responses to the Errai client. Does not handle all possible exceptions.
     *  
     */
    @Bean
    public GlobalResponseEntityExceptionHandler restResponseEntityExceptionHandler() {
        logger.info("Creating RestResponseEntityExceptionHandler");
        return new GlobalResponseEntityExceptionHandler();
    }
        
    @Bean
    public HelloWorldController helloWorldController() {
        logger.info("Creating HelloWorldController");
        return new HelloWorldController();
    }
}

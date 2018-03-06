package com.expansel.errai.spring.sample.config;


import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.annotation.SessionScope;

import com.expansel.errai.spring.sample.bus.ErraiCommandService;
import com.expansel.errai.spring.sample.bus.ErraiMessageCallbackService;
import com.expansel.errai.spring.sample.bus.ErraiRPCServiceImpl;
import com.expansel.errai.spring.sample.service.SpringService;
import com.expansel.errai.spring.server.ErraiApplicationListener;
import com.expansel.errai.spring.server.ErraiRequestDispatcherFactoryBean;
import com.expansel.errai.spring.server.ErraiServerMessageBusFactoryBean;
import com.expansel.errai.springsecurity.server.SpringSecurityMessageCallbackWrapper;

/**
 * Configured in web.xml with contextConfigLocation.
 * 
 * Note we're putting the Errai bus services in the root web app context 
 * because WebConfig is meant for Dispatcher servlet isolated beans.
 * 
 * There is an option to make the bus fall under the DispatcherServlet but
 * have not tried this. Spring has a class called ServletWrappingController 
 * for this purposes. Then bus services would probably better fall under the 
 * specific dispatcher servlet it is registered with. 
 *
 *
 * @author Zach Visagie
 */
@Configuration
@Import({WebSecurityConfig.class, MethodSecurityConfig.class})
@EnableAspectJAutoProxy // need this for @RestrictedAccess on MessageBus, annotation applies to local context only
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    /**
     *  This method needs to be static because it is a BeanFactoryPostProcessor
     */
    @Bean
    public static ErraiApplicationListener erraiApplicationListener() {
        return new ErraiApplicationListener(new SpringSecurityMessageCallbackWrapper());
    }
    
    @Bean
    public FactoryBean<ServerMessageBus> erraiServerMessageBusFactoryBean() {
        logger.info("Creating Errai ServiceMessageBus FactoryBean");
        return new ErraiServerMessageBusFactoryBean();
    }

    @Bean
    public FactoryBean<RequestDispatcher> erraiRequestDispatcherFactoryBean() {
        logger.info("Creating Errai RequestDispatcher FactoryBean");
        return new ErraiRequestDispatcherFactoryBean();
    }

    @Bean
    public ErraiCommandService erraiCommandService() {
        return new ErraiCommandService();
    }

    @Bean
    public ErraiMessageCallbackService erraiMessageCallbackService() {
        return new ErraiMessageCallbackService();
    }

    /**
     * Illustrates an Errai service at session scope. From log messages and RPC 
     * responses can see that the object memory reference is different in different 
     * sessions. Log out and log back in to see in action.
     */
    @Bean
    @SessionScope
    public ErraiRPCServiceImpl erraiRPCService(SpringService springService, RequestDispatcher dispatcher) {
        return new ErraiRPCServiceImpl(springService, dispatcher);
    }

    @Bean
    public SpringService springService() {
        return new SpringService();
    }
}

package com.expansel.errai.spring.sample.gwt.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.InvalidBusContentException;
import org.jboss.errai.security.client.local.handler.SecurityExceptionHandler;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;

import com.google.gwt.user.client.Window;

/**
 * This is to simplify the app to not need to include Errai Navigation and 
 * merely reload on UnauthenticatedExceptions and also display alerts about 
 * what was received. 
 *
 *
 * @author Zach Visagie
 */
@Singleton
@Alternative
public class DemoSecurityExceptionHandler implements SecurityExceptionHandler {
    private static final Logger logger = Logger.getLogger(DemoSecurityExceptionHandler.class.getName());

    @Inject
    private ClientMessageBus bus;

//    @Inject
//    public DemoSecurityExceptionHandler(SecurityContext context, MessageBus bus) {
//      this.context = context;
//      this.bus = bus;
//    }
    
    @Override
    public boolean handleException(Throwable throwable) {
        logger.log(Level.SEVERE, "Alternative SecurityExceptionHandler: " + this, throwable);
        if(throwable == null) {
            Exception e = new Exception();
            logger.log(Level.SEVERE, "Null Exception trace: " + this, e);
            // useful in debugging spring marshalling issues
            Window.alert("Null Exception!!!");
            return false;
        } else if(throwable instanceof UnauthenticatedException) {
            Window.alert("Default UnauthenticatedException!!!");
            Window.Location.reload();
            // prevent follow up communication that would result in additional errors from 
            // since we're already logged out
            logger.info("stopping bus");
            bus.stop(false);
            return false;
        } else if(throwable instanceof InvalidBusContentException) {
            // typically happens when html is replied to the client for example due 
            // to some spring security interception or exception handling. For now
            // keeping it simple and reloading. Look at  GlobalResponseEntityExceptionHandler
            Window.alert("Default InvalidBusContentException!!!");
            Window.Location.reload();
            return false;
        } else if(throwable instanceof UnauthorizedException) {
            Window.alert("Default UnauthorizedException!!!");
            return false;
        }
        return true;
    }
}
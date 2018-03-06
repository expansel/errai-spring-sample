package com.expansel.errai.spring.sample.controller;

import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * <p>This intercepts security exceptions from Spring controllers and writes a json 
 * response to the client side that errai rest clients understand. Currently controllers 
 * don't support @RestrictedAccess so security exceptions are likely to be 
 * AccessDeniedException, unless errai security exceptions are manually thrown.</p>
 * 
 * <p>This is illustrative as there are other exceptions that may occur that should 
 * probably be written to the client side as json. Spring allows per controller 
 * handling of exceptions as well.</p>
 *
 *
 * @author Zach Visagie
 */
@ControllerAdvice
public class GlobalResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalResponseEntityExceptionHandler.class);

    @ExceptionHandler(value = { AccessDeniedException.class, UnauthenticatedException.class, UnauthorizedException.class })
    protected ResponseEntity<Object> handle(RuntimeException ex, WebRequest request) {
        if(ex instanceof AccessDeniedException) {
            ex = new UnauthorizedException();
        } 
        String bodyOfResponse = "{}";
        try {
            bodyOfResponse = ServerMarshalling.toJSON(ex);
        } catch (Exception e) {
            logger.error("Could not convert exception to json", e);
        }
        
        MediaType mediaType = new MediaType("application", "json");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        return handleExceptionInternal(ex, bodyOfResponse, headers, HttpStatus.FORBIDDEN, request);
    }
    
}
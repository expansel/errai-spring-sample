package com.expansel.errai.spring.sample.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expansel.errai.spring.sample.gwt.shared.Greeting;

/**
 * Spring Controller that Errai client side JAX-RS service communicates with to show 
 * possible integration scenarios. It also illustrates Spring security annotations 
 * being applied to eventually write an Errai UnauthorizedException to the client.
 * See {@link GlobalResponseEntityExceptionHandler}
 * 
 *
 * @author Zach Visagie
 */
@RestController
public class HelloWorldController {

    /**
     * <p>Spring uses jackson for json serialization so the Errai client would need
     * to set marshalling to be jackson compatible with:</p>
     * <code> 
     *      RestClient.setJacksonMarshallingActive(true);
     * </code>
     * 
     * <p>There might be a way to intercept the jackson marshalling in Spring and use 
     * Errai marshalling, but this was not investigated. It might be better to just use 
     * JAX-RS instead of Spring MVC on the backend with jersey or resteasy. Note that 
     * ExceptionHandling for security exceptions has not been tested with JAX-RS on the 
     * server.</p>
     * 
     * <p>Note it is best to use Spring security annotations here and not @RestrictedAccess 
     * for which no proxying is supported currently</p>
     * 
     */
    @RequestMapping("/greeting")
    @Secured("admin")
    //@RestrictedAccess(roles= {"admin"}) // This works if adding @EnableAspectJAutoProxy to WebConfig
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(name);
    }
}

package com.expansel.errai.spring.sample.gwt.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.expansel.errai.spring.sample.controller.HelloWorldController;
import com.expansel.errai.spring.sample.gwt.shared.Greeting;

/**
 * This communicates to server side web mvc controller {@link HelloWorldController}
 *
 *
 * @author Zach Visagie
 */
@Path("rest/greeting")
public interface HelloWorldClient {

      @GET
      @Produces("application/json")
      public Greeting retrieveGreeting(@QueryParam("name") String name);
}

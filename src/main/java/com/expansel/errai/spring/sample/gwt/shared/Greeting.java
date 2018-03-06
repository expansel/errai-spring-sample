package com.expansel.errai.spring.sample.gwt.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

import com.expansel.errai.spring.sample.controller.HelloWorldController;

/**
 * Returned from Spring {@link HelloWorldController} and processed by Errai client
 * 
 *
 * @author Zach Visagie
 */
@Portable
public class Greeting {
    private String name;

    public Greeting() {
    }

    public Greeting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
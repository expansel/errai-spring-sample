package com.expansel.errai.spring.sample.config;

import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
/**
 * Illustrates {@link ServerMessageBus} being autowired by spring. As well as 
 * Spring's {@link PreAuthorize} annotation.
 *
 * @author Zach Visagie
 */
public class SpringService {
    private static final Logger logger = LoggerFactory.getLogger(SpringService.class);
    private ServerMessageBus bus;

    @PreAuthorize("hasAuthority('admin')")
    public String ping() {
        logger.info("bus message queues size: " + bus.getMessageQueues().size());
        return "pong";
    }

    @Autowired
    public void setBus(ServerMessageBus bus) {
        // Bus is not yet initialized at this point
        logger.info("Setting ServerMessageBus: " + bus);
        this.bus = bus;
    }
}

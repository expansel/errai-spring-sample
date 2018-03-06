package com.expansel.errai.spring.sample.bus;

import javax.annotation.security.RolesAllowed;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ErraiMessageCallbackService implements MessageCallback {
    private static final Logger logger = LoggerFactory.getLogger(ErraiMessageCallbackService.class);

    @Override
    @RolesAllowed({ "admin" })
    public void callback(Message message) {
        logger.info(this + " - MessageCallback: " + message);
    }

}

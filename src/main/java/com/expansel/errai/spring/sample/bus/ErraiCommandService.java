package com.expansel.errai.spring.sample.bus;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;

@Service
public class ErraiCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ErraiCommandService.class);

    @Command
    // not not using hasRole as we have not prefixed our roles with ROLE_
    @Secured("admin")
    public void command1(Message message) {
        logger.info(this + " - command1");
    }

    /**
     * Illustrate working RequiredRolesProvider implementation.
     * 
     * @param message
     */
    @Command("CMD2")
    @RestrictedAccess(providers=AdminRequiredRolesProvider.class)
    public void command2(Message message) {
        logger.info(this + " - command2 - " + System.identityHashCode(message));
    }

}

package com.expansel.errai.spring.sample.bus;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expansel.errai.spring.sample.config.SpringService;
import com.expansel.errai.spring.sample.gwt.shared.ErraiRPCService;
import com.expansel.errai.spring.sample.gwt.shared.RPCResult;

@Service
public class ErraiRPCServiceImpl implements ErraiRPCService {
    private static final Logger logger = LoggerFactory.getLogger(ErraiRPCServiceImpl.class);

    private SpringService springService;
    private RequestDispatcher dispatcher;

    
    public ErraiRPCServiceImpl(SpringService springService, RequestDispatcher dispatcher) {
        super();
        this.springService = springService;
        this.dispatcher = dispatcher;
    }

    /**
     * Secured on {@link SpringService} which it calls
     */
    @Override
    public RPCResult callPing() {
        logger.info(this + " - callPing");
        RPCResult result = new RPCResult();
        // From printing the default to string you can see the object memory
        // reference and thus see the session scope in action, can change the
        // scope above and recompile to see others in action
        result.setResult(springService.ping() + " - " + this);
        return result;
    }

    /**
     * Using only server side security for illustration
     */
    @Override
    @RestrictedAccess(roles = { "admin" })
    public void triggerMessage() {
        logger.info(this + " - triggerMessage: " + dispatcher);
        MessageBuilder.createMessage().toSubject("MessageFromServer").signalling()
                .with("message", "Passed Admin Authorization").noErrorHandling().sendNowWith(dispatcher);
    }
}

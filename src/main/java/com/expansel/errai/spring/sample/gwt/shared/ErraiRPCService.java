package com.expansel.errai.spring.sample.gwt.shared;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Basic Errai RPC service that will be wired into the MessageBus with spring.
 *
 *
 * @author Zach Visagie
 */
@Remote
public interface ErraiRPCService {

    public RPCResult callPing();

    public void triggerMessage();

}

package com.expansel.errai.spring.sample.config;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.bus.server.servlet.DefaultBlockingServlet;

/**
 * The {@link DefaultBlockingServlet} was not setting the content type to application/json 
 * for session expiry responses. Needs to be fixed in Errai source.
 *
 *
 * @author Zach Visagie
 */
public class FixedBlockingServlet extends DefaultBlockingServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void sendDisconnectDueToSessionExpiry(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        super.sendDisconnectDueToSessionExpiry(response);
    }
}

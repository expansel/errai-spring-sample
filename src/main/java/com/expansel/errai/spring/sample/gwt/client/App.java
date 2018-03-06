package com.expansel.errai.spring.sample.gwt.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.expansel.errai.spring.sample.gwt.shared.ErraiRPCService;
import com.expansel.errai.spring.sample.gwt.shared.Greeting;
import com.expansel.errai.spring.sample.gwt.shared.RPCResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

@EntryPoint
public class App extends Composite {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Inject
    private Caller<ErraiRPCService> messageServiceCaller;

    @Inject
    private MessageBus bus;

    @Inject
    private Caller<AuthenticationService> authenticationServiceCaller;
    
    public static final RestErrorCallback DEFAULT_REST_ERROR_CALLBACK = new RestErrorCallback() {

        @Override
        public boolean error(Request message, Throwable throwable) {
            // Let global exception handlers handle rest errors by default
            // This is useful for having the SecurityExceptionHandler handle
            // JAX-RS exceptions as well
            if(throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        }
    };


    @PostConstruct
    public void postConstruct() {
        bus.subscribe("MessageFromServer", (Message message) -> {
            Window.alert("Message from server: " + message.getParts().get("message"));
        });

    }

    private void initBtns() {
        Button btnRPCPing = new Button("RPC: ping");
        btnRPCPing.setTitle("Call an RPC which illustrates having a dependency wired by spring into an Errai Service");
        btnRPCPing.addClickHandler(event -> {
            messageServiceCaller.call(new RemoteCallback<RPCResult>() {
                @Override
                public void callback(RPCResult result) {
                    Window.alert("Ping result: " + result.getResult());
                }
            }).callPing();
        });
        RootPanel.get().add(btnRPCPing);

        Button btnRPCTrigger = new Button("RPC: trigger");
        btnRPCTrigger.setTitle("RPC with RestrictedAccess for admin");
        btnRPCTrigger.addClickHandler(event -> {
            messageServiceCaller.call(new RemoteCallback<Void>() {
                @Override
                public void callback(Void response) {
                    logger.info("done calling trigger");
                    // this would have trigger bus subscription
                    // MessageFromServer if user had admin role
                }
            }, new ErrorCallback<Message>() {
                @Override
                public boolean error(Message message, Throwable t) {
                    logger.log(Level.SEVERE, "failed to call trigger", t);
                    if (t instanceof UnauthorizedException) {
                        Window.alert("Did not pass admin authorization for RPC call: trigger");
                        return false;
                    } 
                    return true;
                }
            }).triggerMessage();
        });
        RootPanel.get().add(btnRPCTrigger);

        Button btnMessageCallback = new Button("MessageCallback");
        btnMessageCallback.setTitle("See log statements for printed message from MessageCallback implementation");
        btnMessageCallback.addClickHandler(event -> {
            MessageBuilder.createMessage().toSubject("ErraiMessageCallbackService").signalling().defaultErrorHandling()
                    .sendNowWith(ErraiBus.getDispatcher());
        });
        RootPanel.get().add(btnMessageCallback);

        Button btnCommand1 = new Button("CommandService: command1");
        btnCommand1.setTitle("See log statements for printed message from Service with @Command annotated methods: command name equals method name");
        btnCommand1.addClickHandler(event -> {
            MessageBuilder.createMessage().toSubject("ErraiCommandService").command("command1").defaultErrorHandling()
                    .sendNowWith(ErraiBus.getDispatcher());
        });
        RootPanel.get().add(btnCommand1);

        Button btnCommand2 = new Button("CommandService: command2");
        btnCommand2.setTitle("See log statements for printed message from Service with @Command annotated methods: command name specified in annotation");
        btnCommand2.addClickHandler(event -> {
            MessageBuilder.createMessage().toSubject("ErraiCommandService").command("CMD2").defaultErrorHandling()
                    .sendNowWith(ErraiBus.getDispatcher());
        });
        RootPanel.get().add(btnCommand2);

        // The REST call illustrates accessing a spring REST controller
        // it forces the duplication of the interface in JAX-RS, but 
        // seemed like an interesting example :)
          
        // server side json generated by jackson
        RestClient.setJacksonMarshallingActive(true);
        Button btnRESTCall = new Button("Spring Controller via REST");
        btnRESTCall.setTitle("Calls a Spring REST service which returns jackson json");
        btnRESTCall.addClickHandler(handler -> {
            RestClient.create(HelloWorldClient.class, (Greeting greeting) -> {
                Window.alert("Hello " + greeting.getName());
            }, DEFAULT_REST_ERROR_CALLBACK).retrieveGreeting("Spring");
        });
        RootPanel.get().add(btnRESTCall);

        // logout is probably best handled with SpringSecurity logout url
        // but using auth service for example
        Button btnLogout = new Button("Logout");
        btnLogout.addClickHandler(event -> {
            authenticationServiceCaller.call().logout();
        });
        RootPanel.get().add(btnLogout);
    }

    @AfterInitialization
    public void afterInitialization() {
        authenticationServiceCaller.call(new RemoteCallback<User>() {
            @Override
            public void callback(User user) {
                Label userLabel = new Label(
                        "User logged in: username=" + user.getIdentifier() + ", roles=" + user.getRoles());
                RootPanel.get().add(userLabel);
                SimplePanel panel = new SimplePanel();
                Anchor logoutLink = new Anchor("Spring Security Logout URL", GWT.getHostPageBaseURL() + "logout");
                panel.add(logoutLink);
                RootPanel.get().add(panel);
                initBtns();
            }
        }).getUser();
    }

}

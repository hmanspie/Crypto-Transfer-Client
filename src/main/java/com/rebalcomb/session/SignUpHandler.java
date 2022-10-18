package com.rebalcomb.session;

import com.rebalcomb.model.dto.AccountSignInRequest;
import com.rebalcomb.model.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;

public class SignUpHandler extends StompSessionHandlerAdapter {
    private Logger logger = LogManager.getLogger(LoginHandler.class);

    public static Account account;
    public static Boolean isSave = false;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/signUp", this);
        logger.info("Subscribed to /topic/signUp");
        session.send("/app/registered", account);
        logger.info(session.isConnected());
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Boolean.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        isSave = (Boolean) payload;
        logger.info("Save : " + isSave);
    }
}

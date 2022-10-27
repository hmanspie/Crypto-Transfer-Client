package com.rebalcomb.session;

import com.rebalcomb.model.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

public class SignUpHandler extends StompSessionHandlerAdapter {
    private final Logger logger = LogManager.getLogger(LoginHandler.class);
    public static Account account;
    public static String END_POINT = "/app/registered/";
    public static Boolean isSave = false;
    private final CountDownLatch latch;
    public SignUpHandler(final CountDownLatch latch) {
        this.latch = latch;
    }
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        String sessionId = session.getSessionId();
        logger.info("New session established : " + sessionId);
        session.subscribe("/topic/signUp/" + sessionId, this);
        logger.info("Subscribed to /topic/signUp/" + sessionId);
        session.send(END_POINT + "" + sessionId, account);
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
        try {
            isSave = (Boolean) payload;
            logger.info("Save : " + isSave);
        }finally {
            latch.countDown();
        }
    }

}

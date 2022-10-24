package com.rebalcomb.session;

import com.rebalcomb.controllers.AccountController;
import com.rebalcomb.model.dto.KeyPairRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;


public class KeyGenerateHandler extends StompSessionHandlerAdapter {
    private Logger logger = LogManager.getLogger(KeyGenerateHandler.class);
    private final CountDownLatch latch;
    public static String END_POINT = "/app/keyGenerate/";
    public KeyGenerateHandler(final CountDownLatch latch) {
        this.latch = latch;
    }
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        String sessionId = session.getSessionId();
        logger.info("New session established : " + sessionId);
        session.subscribe("/topic/getPublicKey/" + sessionId, this);
        logger.info("Subscribed to /topic/getPublicKey/" + sessionId);
        session.send(END_POINT + "" + sessionId, AccountController.activeAccount.getLogin());

    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return  KeyPairRequest.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            AccountController.KEY_PAIR = ((KeyPairRequest) payload);
            logger.info("Get public key seccessfully!");
        }finally {
            latch.countDown();
        }
    }
}

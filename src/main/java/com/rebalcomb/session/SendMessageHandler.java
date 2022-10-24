package com.rebalcomb.session;

import com.rebalcomb.model.dto.BlockRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

public class SendMessageHandler extends StompSessionHandlerAdapter {
    private final Logger logger = LogManager.getLogger(SendMessageHandler.class);
    public static Boolean isSend = false;
    public static String END_POINT = "/app/sendMessage/";
    private final CountDownLatch latch;
    public SendMessageHandler(final CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        String sessionId = session.getSessionId();
        logger.info("New session established : " + sessionId);
        session.subscribe("/topic/getResultSent/" + sessionId, this);
        logger.info("Subscribed to /topic/getResultSent/" + sessionId);
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
        try {
            isSend = (Boolean) payload;
            logger.info("Send: " + isSend);
        }finally {
            latch.countDown();
        }
    }
}

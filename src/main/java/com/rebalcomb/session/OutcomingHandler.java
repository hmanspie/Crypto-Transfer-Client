package com.rebalcomb.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import java.lang.reflect.Type;


public class OutcomingHandler extends StompSessionHandlerAdapter {
    private Logger logger = LogManager.getLogger(OutcomingHandler.class);
    public static String END_POINT = "/app/outcomingMessage/";
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        String sessionId = session.getSessionId();
        logger.info("New session established : " + sessionId);
        session.subscribe("/topic/getOutcoming/" + sessionId, this);
        logger.info("Subscribed to /topic/getOutcoming/" + sessionId);
        session.send(END_POINT + "" + sessionId, sessionId);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return  Boolean.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Boolean out = (Boolean) payload;
        logger.info("Outcoming message get successfully! " + out);

    }
}

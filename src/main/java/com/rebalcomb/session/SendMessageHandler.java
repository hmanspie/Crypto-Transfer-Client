package com.rebalcomb.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebalcomb.model.dto.NewMessageRequest;
import com.rebalcomb.model.entity.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.List;

public class SendMessageHandler extends StompSessionHandlerAdapter {
    private Logger logger = LogManager.getLogger(SendMessageHandler.class);

    public static NewMessageRequest newMessageRequest;
    public static Boolean isSend = false;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/getResultSent", this);
        logger.info("Subscribed to /topic/getResultSent");
        session.send("/app/sendMessage", newMessageRequest);
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
        isSend = (Boolean) payload;
        logger.info("Sent: " + isSend);
    }
}

package com.rebalcomb.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebalcomb.model.entity.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.List;

public class OutcomingHandler extends StompSessionHandlerAdapter {
    private Logger logger = LogManager.getLogger(OutcomingHandler.class);

    public static List<Message> messageList;
    public static String sender;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/getOutcoming", this);
        logger.info("Subscribed to /topic/getOutcoming");
        session.send("/app/outcomingMessage", sender);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return  new TypeReference<List<Message>>(){}.getType();
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        messageList = new ObjectMapper().convertValue((List<Message>) payload, new TypeReference<List<Message>>(){});
        logger.info("Outcoming message get successfully! count: " + messageList.size());
    }
}

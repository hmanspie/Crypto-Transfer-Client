package com.rebalcomb.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rebalcomb.controllers.AccountController;
import com.rebalcomb.crypto.AESUtil;
import com.rebalcomb.crypto.rsa.RSAUtil;
import com.rebalcomb.model.dto.AccountSecretKey;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.service.MessageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class IncomingHandler extends StompSessionHandlerAdapter {
    private Logger logger = LogManager.getLogger(IncomingHandler.class);
    public static String END_POINT = "/app/incomingMessage/";
    public List<MessageRequest> incomingMessageList = new ArrayList<>();
    public List<MessageRequest> messageList = new ArrayList<>();
    private Gson gson = new Gson();
    private final Type listOfMyClassObject = new TypeToken<List<MessageRequest>>() {}.getType();


    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        String sessionId = session.getSessionId();
        logger.info("New session established : " + sessionId);
        session.subscribe("/topic/getIncoming/" + sessionId, this);
        logger.info("Subscribed to /topic/getIncoming/" + sessionId);
        AccountSecretKey accountSecretKey = new AccountSecretKey();
        accountSecretKey.setLogin(AccountController.activeAccount.getLogin());
        accountSecretKey.setSecret(RSAUtil.decrypt(AESUtil.SECRET_KEY,
                                        AccountController.KEY_PAIR.getPublicKey(),
                                                AccountController.KEY_PAIR.getModule()));
        session.send(END_POINT + "" + sessionId, accountSecretKey);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return  new TypeReference<List<MessageRequest>>(){}.getType();
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        incomingMessageList = new ObjectMapper().convertValue((List<MessageRequest>) payload, new TypeReference<List<MessageRequest>>() {});
        logger.info("Incoming message get successfully! count: " + incomingMessageList.size());
        messageList.clear();
        for (MessageRequest messageRequest : incomingMessageList) {
            messageRequest.setBodyMessage(AESUtil.decrypt(messageRequest.getBodyMessage()));
            messageRequest.setId((long) (messageList.size() + 1));
            messageList.add(messageRequest);
        }
        String listJson = null;
        try {
            listJson = MessageService.readFile(MessageService.INCOMING_MESSAGES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<MessageRequest> outputList = gson.fromJson(listJson, listOfMyClassObject);
        if(outputList != null) {
            for (MessageRequest messageRequest : outputList) {
                messageRequest.setId((long) (messageList.size() + 1));
                messageList.add(messageRequest);
            }
        }
        try {
            MessageService.writeFile(gson.toJson(messageList), MessageService.INCOMING_MESSAGES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

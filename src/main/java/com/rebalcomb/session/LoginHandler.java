package com.rebalcomb.session;

import com.rebalcomb.controllers.AccountController;
import com.rebalcomb.model.dto.AccountSignInRequest;
import com.rebalcomb.model.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

public class LoginHandler extends StompSessionHandlerAdapter {
    private final Logger logger = LogManager.getLogger(LoginHandler.class);
    public static AccountSignInRequest accountSignInRequest;
    public Boolean isAccess = false;
    public static String END_POINT = "/app/service/";
    private final CountDownLatch latch;
    public LoginHandler(final CountDownLatch latch) {
        this.latch = latch;
    }
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        String sessionId = session.getSessionId();
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/signIn/" + sessionId, this);
        logger.info("Subscribed to /topic/signIn/" + sessionId);
        session.send(END_POINT + "" + sessionId, accountSignInRequest);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Account.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            AccountController.activeAccount = (Account) payload;
            if(AccountController.activeAccount != null)
                isAccess = true;
            logger.info("Access : " + isAccess);
        }finally {
            latch.countDown();
        }
    }
}

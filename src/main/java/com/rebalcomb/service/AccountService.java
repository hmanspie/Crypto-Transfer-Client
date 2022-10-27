package com.rebalcomb.service;

import com.rebalcomb.crypto.Hash;
import com.rebalcomb.mapper.AccountMapper;
import com.rebalcomb.model.dto.AccountSignInRequest;
import com.rebalcomb.model.dto.AccountSignUpRequest;
import com.rebalcomb.session.KeyGenerateHandler;
import com.rebalcomb.session.LoginHandler;
import com.rebalcomb.session.SignUpHandler;
import com.rebalcomb.socket.StompClient;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CountDownLatch;


@Service
public class AccountService {

    private ListenableFuture<StompSession> stompSessionOutcoming;

    public Boolean isAccess(AccountSignInRequest accountSignInRequest) throws InterruptedException {
        LoginHandler.accountSignInRequest = new AccountSignInRequest(accountSignInRequest.getLogin(), Hash.getSHA512(accountSignInRequest.getPasswd()));
        CountDownLatch latch = new CountDownLatch(1);
        LoginHandler loginHandler = new LoginHandler(latch);
        StompSessionHandler sessionHandler = loginHandler;
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_SERVICE, sessionHandler);
        latch.await();
        return loginHandler.isAccess;
    }

    public Boolean validatePassword(AccountSignUpRequest request) {
        if (request.getPasswd().equals(request.getConfirmPassword())) {
            return true;
        }
        return false;
    }

    public Boolean saveUser(AccountSignUpRequest request) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SignUpHandler.account = AccountMapper.mapAccountRequest(request);
        StompSessionHandler sessionHandler = new SignUpHandler(latch);
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_REGISTERED, sessionHandler);
        latch.await();
        return SignUpHandler.isSave;
    }

    public void getPublicKey() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StompSessionHandler sessionHandler = new KeyGenerateHandler(latch);
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_KEYGENERATE, sessionHandler);
        latch.await();
    }
}


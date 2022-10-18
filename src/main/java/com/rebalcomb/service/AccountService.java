package com.rebalcomb.service;

import com.rebalcomb.crypto.Hash;
import com.rebalcomb.mapper.AccountMapper;
import com.rebalcomb.model.dto.AccountSignInRequest;
import com.rebalcomb.model.dto.AccountSignUpRequest;
import com.rebalcomb.session.LoginHandler;
import com.rebalcomb.session.SignUpHandler;
import com.rebalcomb.socket.StompClient;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.WebSocketStompClient;


@Service
public class AccountService {

    public static String URL = "ws://localhost:8080/service";
    public Boolean isAccess(AccountSignInRequest accountSignInRequest) throws InterruptedException {
        LoginHandler.accountSignInRequest = new AccountSignInRequest(accountSignInRequest.getLogin(), Hash.getSHA512(accountSignInRequest.getPasswd()));
        StompSessionHandler sessionHandler = new LoginHandler();
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_SERVICE, sessionHandler);
        do {Thread.sleep(500);
        } while (stompClient.isRunning());
        return LoginHandler.isAccess;
    }

    public Boolean validatePassword(AccountSignUpRequest request) {
        if (request.getPasswd().equals(request.getConfirmPassword())) {
            return true;
        }
        return false;
    }

    public Boolean saveUser(AccountSignUpRequest request) throws InterruptedException {
        SignUpHandler.account = AccountMapper.mapAccountRequest(request);
        StompSessionHandler sessionHandler = new SignUpHandler();
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_REGISTERED, sessionHandler);
        do {Thread.sleep(500);
        } while (stompClient.isRunning());
        return SignUpHandler.isSave;
    }
}


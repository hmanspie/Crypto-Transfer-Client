package com.rebalcomb.service;

import com.rebalcomb.controllers.AccountController;
import com.rebalcomb.model.dto.NewMessageRequest;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.session.IncomingHandler;
import com.rebalcomb.session.OutcomingHandler;
import com.rebalcomb.session.SendMessageHandler;
import com.rebalcomb.socket.StompClient;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.List;

@Service
public class MessageService {


    public List<Message> findAllBySender(String sender) throws InterruptedException {
        IncomingHandler.sender = sender;
        StompSessionHandler sessionHandler = new IncomingHandler();
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_INCOMING, sessionHandler);
        do {Thread.sleep(500);
        } while (stompClient.isRunning());
        return IncomingHandler.messageList;
    }

    public List<Message> findAllByRecipient(String sender) throws InterruptedException {
        OutcomingHandler.sender = sender;
        StompSessionHandler sessionHandler = new OutcomingHandler();
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_OUTCOMING, sessionHandler);
        do {Thread.sleep(500);
        } while (stompClient.isRunning());
        return OutcomingHandler.messageList;
    }

    public Boolean sendMessage(NewMessageRequest newMessageRequest) throws InterruptedException {
        SendMessageHandler.newMessageRequest = newMessageRequest;
        SendMessageHandler.newMessageRequest.setFrom(AccountController.activeAccount);
        StompSessionHandler sessionHandler = new SendMessageHandler();
        WebSocketStompClient stompClient = new StompClient().getWebSocket();
        stompClient.connect(StompClient.URL_SEND, sessionHandler);
        do {Thread.sleep(500);
        } while (stompClient.isRunning());
        return SendMessageHandler.isSend;
    }
}

package com.rebalcomb.socket;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class StompClient {

    public static final String URL_SERVICE = "ws://localhost:8080/service";
    public static final String URL_REGISTERED = "ws://localhost:8080/registered";
    public static final String URL_INCOMING = "ws://localhost:8080/incomingMessage";
    public static final String URL_OUTCOMING = "ws://localhost:8080/outcomingMessage";

    public static final String URL_SEND = "ws://localhost:8080/sendMessage";
    private WebSocketClient client = new StandardWebSocketClient();
    private WebSocketStompClient stompClient = new WebSocketStompClient(client);

    {
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }
    public WebSocketStompClient getWebSocket(){
        return stompClient;
    }
}

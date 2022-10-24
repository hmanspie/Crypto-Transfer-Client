package com.rebalcomb.socket;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.util.ArrayList;
import java.util.List;

public class StompClient {

    public static final String URL_SERVICE = "ws://localhost:8080/service";
    public static final String URL_REGISTERED = "ws://localhost:8080/registered";
    public static final String URL_INCOMING = "ws://localhost:8080/incomingMessage";
    public static final String URL_OUTCOMING = "ws://localhost:8080/outcomingMessage";
    public static final String URL_SEND = "ws://localhost:8080/sendMessage";
    public static final String URL_KEYGENERATE = "ws://localhost:8080/keyGenerate";
    private WebSocketClient webSocketClient;
    private WebSocketStompClient stompClient;
    List<Transport> transports = new ArrayList<>();
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    {
        container.setDefaultMaxBinaryMessageBufferSize(1024 * 1024);
        container.setDefaultMaxTextMessageBufferSize(1024 * 1024);
        transports.add(new WebSocketTransport(new StandardWebSocketClient(container)));
        webSocketClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setInboundMessageSizeLimit(1024 * 1024 * 1024);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }
    public WebSocketStompClient getWebSocket(){
        return stompClient;
    }
}

package com.rebalcomb.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rebalcomb.controllers.AccountController;
import com.rebalcomb.crypto.AESUtil;
import com.rebalcomb.crypto.rsa.RSAUtil;
import com.rebalcomb.model.dto.AccountSecretKey;
import com.rebalcomb.model.dto.BlockRequest;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.session.IncomingHandler;
import com.rebalcomb.session.OutcomingHandler;
import com.rebalcomb.session.SendMessageHandler;
import com.rebalcomb.socket.StompClient;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Service
public class MessageService {
    private Gson gson = new Gson();
    public static final String OUTCOMING_MESSAGES = "outcomingMessages.txt";
    public static final String INCOMING_MESSAGES = "incomingMessages.txt";
    public static final String IMAGES = "images.txt";
    private final Type listOfMyClassObject = new TypeToken<List<MessageRequest>>() {}.getType();

    private ListenableFuture<StompSession> stompSessionSend;
    private ListenableFuture<StompSession> stompSessionIncoming;
    private Thread threadOutcoming;
    private Thread threadIncoming;
    private CountDownLatch latch;
    public List<MessageRequest> findAllByRecipient() throws IOException, InterruptedException {
        if(threadIncoming == null) {
            incomingListener();
        }
        return gson.fromJson(readFile(INCOMING_MESSAGES), listOfMyClassObject);
    }

    public void incomingListener(){
        threadIncoming = new Thread(() -> {
            try {
                do {
                    if (stompSessionIncoming != null && stompSessionIncoming.get().isConnected()) {
                        AccountSecretKey accountSecretKey = new AccountSecretKey();
                        accountSecretKey.setLogin(AccountController.activeAccount.getLogin());
                        accountSecretKey.setSecret(RSAUtil.decrypt(AESUtil.SECRET_KEY,
                                                        AccountController.KEY_PAIR.getPublicKey(),
                                                            AccountController.KEY_PAIR.getModule()));
                        stompSessionIncoming.get().send(IncomingHandler.END_POINT + "" +
                                                                        stompSessionIncoming.get().getSessionId(), accountSecretKey);
                    } else {
                        StompSessionHandler sessionHandler = new IncomingHandler();
                        WebSocketStompClient stompClient = new StompClient().getWebSocket();
                        stompSessionIncoming = stompClient.connect(StompClient.URL_INCOMING, sessionHandler);
                    }
                    Thread.sleep(10000);
                }
                while (true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        threadIncoming.start();
    }
    public List<MessageRequest> findAllBySender() throws IOException, ExecutionException, InterruptedException {
        return gson.fromJson(readFile(OUTCOMING_MESSAGES), listOfMyClassObject);
    }

    public Boolean sendMessage(BlockRequest blockRequest) throws IOException, InterruptedException, ExecutionException {
        String listJson = readFile(OUTCOMING_MESSAGES);
        List<MessageRequest> outputList = gson.fromJson(listJson, listOfMyClassObject);
        if(outputList != null) {
            blockRequest.getMessageRequest().setId((long) outputList.size() + 1);
            outputList.add(blockRequest.getMessageRequest());
        }
        else {
            blockRequest.getMessageRequest().setId(1L);
            outputList = new ArrayList<>();
            outputList.add(blockRequest.getMessageRequest());
        }
        String jsonString = gson.toJson(outputList);
        writeFile(jsonString, OUTCOMING_MESSAGES);
        blockRequest.getMessageRequest().setBodyMessage(AESUtil.encrypt(blockRequest.getMessageRequest().getBodyMessage()));
        blockRequest.getMessageRequest().setFrom(AccountController.activeAccount.getLogin());
        if (stompSessionSend != null && stompSessionSend.get().isConnected()) {
            stompSessionSend.get().send(SendMessageHandler.END_POINT + "" +
                    stompSessionSend.get().getSessionId(), blockRequest);
        } else {
            latch = new CountDownLatch(1);
            StompSessionHandler sessionHandler = new SendMessageHandler(latch);
            WebSocketStompClient stompClient = new StompClient().getWebSocket();
            stompSessionSend = stompClient.connect(StompClient.URL_SEND, sessionHandler);
            stompSessionSend.get().send(SendMessageHandler.END_POINT + "" +
                    stompSessionSend.get().getSessionId(), blockRequest);

        }
        latch.await();
        return SendMessageHandler.isSend;
    }

    public static List<String> getRandomImageList(Integer count) throws IOException {
        String output = readFile(IMAGES);
        String [] imageArray = output.split("\r\n");
            List<String> imageList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                imageList.add(imageArray[(int) (Math.random() * imageArray.length)]);
            } return imageList;
    }
    public static void writeFile(String text, String file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(text);
        fileWriter.close();
    }
    public static String readFile(String file) throws IOException {
        Path fileName = Path.of(file);
        return Files.readString(fileName);
    }

}

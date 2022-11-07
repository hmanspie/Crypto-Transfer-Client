package com.rebalcomb.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebalcomb.crypto.AESUtil;
import com.rebalcomb.crypto.Hiding;
import com.rebalcomb.io.File;
import com.rebalcomb.mapper.MessageMapper;
import com.rebalcomb.model.dto.SecretBlock;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.repository.MessageRopository;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageService {
    private final UserService userService;
    private final MessageRopository messageRepository;
    public static final String IMAGES = "images.txt";
    private RSocketRequester requester;
    private final RSocketRequester.Builder builder;

    @Autowired
    public MessageService(UserService userService, MessageRopository messageRepository, RSocketRequester.Builder builder) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.builder = builder;
        this.requester = this.builder
                                .transport(TcpClientTransport
                                        .create("localhost", 7000));
        incomingListener();
    }
    public Flux<Message> findAll(){
        Flux<Message> flux = this.requester
                            .route("message.findAll")
                            .retrieveFlux(Message.class);
        return flux;
    }

    public Mono<Boolean> send(SecretBlock secretBlock) {
        return this.requester
                .route("message.send")
                .data(secretBlock)
                .retrieveMono(Boolean.class);
    }
    public Flux<MessageRequest> getIncoming(String serverId) {
        return this.requester
                .route("message.getIncoming")
                .data(serverId)
                .retrieveFlux(MessageRequest.class);
    }

    public List<Message> findAllByRecipient(String username) {
        return findAllByUsernameTo(username);
    }

    public List<Message> findAllBySender(String username) {
        return findAllByUsernameFrom(username);
    }

    public SecretBlock encryptMessage(SecretBlock secretBlock){
        String hiding = new Hiding().generateHidingMassage(AESUtil.encrypt(secretBlock.getMessageRequest().getBodyMessage(), userService.findSecretByUsername(secretBlock.getMessageRequest().getUser_from())));
        secretBlock.getMessageRequest().setBodyMessage(hiding);
        return secretBlock;
    }
    public Message decryptMessage(Message message) {
        String secret = userService.findSecretByUsername(message.getUser_to());
        String hidingMessage = new Hiding().getOpenMassageForHidingMassage(message.getBody());
        message.setBody(AESUtil.decrypt(hidingMessage, secret));
        return message;
    }

    public void saveMessage(MessageRequest messageRequest){
        Message message = MessageMapper.mapMessage(messageRequest);
        save(decryptMessage(message));
    }

    public void incomingListener(){
        Thread threadIncoming = new Thread(() -> {
            try {
                do {
                    List<MessageRequest> messageRequests = getIncoming("1").collectList().block();
                    if(messageRequests != null)
                    for (MessageRequest messageRequest : messageRequests)
                        saveMessage(messageRequest);
                    Thread.sleep(5000);
                }
                while (true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        threadIncoming.start();
    }

    public Boolean sendMessage(SecretBlock secretBlock) {
        Optional<User> to = userService.findByUsername(secretBlock.getMessageRequest().getUser_to());
        if (to.isPresent()) {
            return send(encryptMessage(secretBlock)).block();
        }
        return false;
    }

    public static List<String> getRandomImageList(Integer count) throws IOException {
        String output = File.readFile(IMAGES);
        String [] imageArray = output.split("\r\n");
            List<String> imageList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                imageList.add(imageArray[(int) (Math.random() * imageArray.length)]);
            } return imageList;
    }

    public List<Message> findAllByUsernameFrom(String username){
        return messageRepository.findAllByUsernameFrom(username);
    }

    public List<Message> findAllByUsernameTo(String username){
        return messageRepository.findAllByUsernameTo(username);
    }

    public Message findTopByOrderByIdDesc(){
        return messageRepository.findTopByOrderByIdDesc();
    }
    public List<String> findAllUsername(){
        return userService.findAllUsername();
    }
    public void save(Message message){
        messageRepository.save(message);
    }


}

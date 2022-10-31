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
import com.rebalcomb.util.LocalDateTimeDeserializer;
import com.rebalcomb.util.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final RSocketService rSocketService;
    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson;
    @Autowired
    public MessageService(UserService userService, MessageRopository messageRepository, RSocketService rSocketService) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.rSocketService = rSocketService;
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gson = gsonBuilder.setPrettyPrinting().create();
        incomingListener();
    }

    public List<Message> findAllByRecipient(String username) {
        return findAllByUsernameTo(username);
    }

    public List<Message> findAllBySender(String username) {
        return findAllByUsernameFrom(username);
    }

    public SecretBlock encryptMessage(SecretBlock secretBlock){
        String hiding = new Hiding().generateHidingMassage(AESUtil.encrypt(secretBlock.getMessageRequest().getBodyMessage(), userService.findSecretByUsername(secretBlock.getMessageRequest().getFrom())));
        secretBlock.getMessageRequest().setBodyMessage(hiding);
        return secretBlock;
    }
    public Message decryptMessage(Message message) {
        String secret = message.getTo().getSecret();
        String hidingMessage = new Hiding().getOpenMassageForHidingMassage(message.getBody());
        message.setBody(AESUtil.decrypt(hidingMessage, secret));
        return message;
    }

    public void saveMessage(MessageRequest messageRequest){
        Message message = MessageMapper.mapMessage(messageRequest,
                userService.findByUsername(messageRequest.getFrom()).get(),
                userService.findByUsername(messageRequest.getTo()).get());
        save(decryptMessage(message));
    }

    public void incomingListener(){
        Thread threadIncoming = new Thread(() -> {
            try {
                do {
                    for (MessageRequest messageRequest : rSocketService.getIncoming("1")) {
                        saveMessage(messageRequest);
                    }
                    Thread.sleep(5000);
                }
                while (true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        threadIncoming.start();
    }

    public Message secretBlockConvertToMessage(SecretBlock secretBlock, User user){
        return MessageMapper.mapMessage(secretBlock.getMessageRequest(),
                userService.findByUsername(secretBlock
                        .getMessageRequest().getFrom()).get(), user);
    }

    // todo потрібно зробити попередження про те що адресата не знайдено
    public Boolean sendMessage(SecretBlock secretBlock) {
        Optional<User> to = userService.findByUsername(secretBlock.getMessageRequest().getTo());
        if (to.isPresent()) {
            //save(secretBlockConvertToMessage(secretBlock, userService.findByUsername(to.get().getUsername()).get()));
            return Boolean.parseBoolean(rSocketService.sendMessage(encryptMessage(secretBlock)));
        } else {
            User user = rSocketService.searchUserInMainServer(secretBlock.getMessageRequest().getTo());
            if (user != null) {
                user.getIncomingMessageList().clear();
                userService.save(user);
                save(secretBlockConvertToMessage(secretBlock, userService.findByUsername(user.getUsername()).get()));
                return Boolean.parseBoolean(rSocketService.sendMessage(encryptMessage(secretBlock)));
            }
        } return false;
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

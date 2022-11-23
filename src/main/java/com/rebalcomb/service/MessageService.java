package com.rebalcomb.service;

import com.rebalcomb.config.ServerUtil;
import com.rebalcomb.crypto.AESUtil;
import com.rebalcomb.crypto.Hash;
import com.rebalcomb.crypto.Hiding;
import com.rebalcomb.io.File;
import com.rebalcomb.mapper.MessageMapper;
import com.rebalcomb.model.dto.Block;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.entity.Certificate;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.repositories.MessageRopository;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageService {
    private final UserService userService;
    private final MessageRopository messageRepository;

    private final CertificateService certificateService;
    public static final String IMAGES = "images.txt";
    private RSocketRequester requester;
    private Thread threadIncoming;
    private final RSocketRequester.Builder builder;
    private final Logger logger = LogManager.getLogger(MessageService.class);

    @Autowired
    public MessageService(UserService userService, MessageRopository messageRepository, CertificateService certificateService, RSocketRequester.Builder builder) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.certificateService = certificateService;
        this.builder = builder;
    }

    public void requesterInitialization(){
        this.requester = this.builder
                .rsocketConnector(c -> c.reconnect(Retry.fixedDelay(100, Duration.ofSeconds(5))
                        .doBeforeRetry(l -> logger.warn("Retrying " + (l.totalRetriesInARow() + 1) + " connected to remote server!"))
                        .doAfterRetry(l -> logger.warn("No connection setup with the remote server!"))))
                .transport(TcpClientTransport
                        .create(ServerUtil.REMOTE_SERVER_IP_ADDRESS, ServerUtil.REMOTE_SERVER_PORT));
       incomingListener();
    }
    public Flux<Message> findAll(){
        Flux<Message> flux = this.requester
                            .route("message.findAll")
                            .retrieveFlux(Message.class);
        return flux;
    }

    public Mono<Boolean> send(Block block) {
        return this.requester
                .route("message.send")
                .data(block)
                .retrieveMono(Boolean.class);
    }
    public Flux<Block> getIncoming(String serverId) {
        return this.requester
                .route("message.getIncoming")
                .data(serverId)
                .retrieveFlux(Block.class);
    }

    public List<Message> findAllByRecipient(String username) {
        return findAllByUsernameTo(username);
    }

    public List<Message> findAllBySender(String username) {
        return findAllByUsernameFrom(username);
    }

    public String encryptMessage(MessageRequest messageRequest){
        return new Hiding().generateHidingMassage(AESUtil.encrypt(messageRequest, userService.findSecretByUsername(messageRequest.getUser_from())));
    }
    public Message decryptMessage(Block block) {
        String hidingMessage = new Hiding().getOpenMassageForHidingMassage(block.getMessage());
        MessageRequest messageRequest = AESUtil.decrypt(hidingMessage, userService.findSecretByUsername(block.getFrom()));
        Message message = MessageMapper.mapMessage(messageRequest, block.getHash());
        Certificate certificate = MessageMapper.mapCertifacate(messageRequest, userService.findSecretByUsername(messageRequest.getUser_from()));
        certificate = certificateService.save(certificate);
        message.setCertificate(certificate);
        return message;
    }

    public void saveMessage(Block block){
        save(decryptMessage(block));
    }

    public void incomingListener() {
        threadIncoming = new Thread(() -> {
            do {
                try {
                    Thread.sleep(5000);
                    List<Block> blockList = getIncoming(ServerUtil.SERVER_ID).collectList().block();
                    if (blockList != null)
                        for (Block block : blockList)
                            saveMessage(block);
                } catch (Exception e) {
                    logger.error(e);
                }
            } while (true);
        });
        threadIncoming.start();
    }

    public Boolean sendMessage(MessageRequest messageRequest) throws NoSuchAlgorithmException {
        Optional<User> to = userService.findByUsername(messageRequest.getUser_to());
        if (to.isPresent()) {
            Block block = new Block();
            block.setFrom(messageRequest.getUser_from());
            block.setTo(messageRequest.getUser_to());
            block.setMessage(encryptMessage(messageRequest));
            block.setHash(Hash.getHash(messageRequest.getBodyMessage()));
            return send(block).block();
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

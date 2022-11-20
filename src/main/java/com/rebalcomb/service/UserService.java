package com.rebalcomb.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rebalcomb.config.ServerUtil;
import com.rebalcomb.controllers.UserController;
import com.rebalcomb.crypto.RSAUtil;
import com.rebalcomb.mapper.UserMapper;
import com.rebalcomb.model.dto.ChangeSecretRequest;
import com.rebalcomb.model.dto.SignUpRequest;
import com.rebalcomb.model.dto.UpdateRequest;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.repositories.UserRepository;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private RSocketRequester requester;
    private Thread threadUpdateUsers;
    private Thread threadUpdateSalt;
    private Thread threadUpdateEncryptMode;
    private RSAUtil rsaUtil;
    private Thread threadUpdateIV;
    private final RSocketRequester.Builder builder;
    private final Logger logger = LogManager.getLogger(UserService.class);
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public UserService(UserRepository userRepository, RSocketRequester.Builder builder) throws NoSuchAlgorithmException {
        rsaUtil  = new RSAUtil();
        this.builder = builder;
        this.requester = this.builder
                .rsocketConnector(c -> c.reconnect(Retry.fixedDelay(100, Duration.ofSeconds(5))
                        .doBeforeRetry(l -> logger.warn("Waiting for connection to remote server!"))))
                .transport(TcpClientTransport
                        .create(ServerUtil.REMOTE_SERVER_IP_ADDRESS, ServerUtil.REMOTE_SERVER_PORT));
        this.userRepository = userRepository;

        if (ServerUtil.PUBLIC_KEY == null) {
            try {
                getPublicKeyFromRemoteServer();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
        updateUsersTable();
        updateSalt();
        updateIV();
        updateEncryptMode();
    }

    public void getPublicKeyFromRemoteServer() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyBase64 = getPublicKey(ServerUtil.SERVER_ID).block();
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        ServerUtil.PUBLIC_KEY = keyFactory.generatePublic(publicKeySpec);
        logger.info("Get public key: " + publicKeyBase64 + " successfully!");
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByUsername(email);
    }


    public List<User> findAll(){ return userRepository.findAll(); }

    public Mono<String> getPublicKey(String serverId) {
        Mono<String> mono = this.requester
                .route("server.getPublicKey")
                .data(serverId)
                .retrieveMono(String.class);
        return mono;
    }

    public Mono<String> getSalt(UpdateRequest updateRequest){
        Mono<String> mono = this.requester
                .route("server.getSalt")
                .data(updateRequest)
                .retrieveMono(String.class);
        return mono;
    }

    public Mono<String> getEncryptMode(String serverID){
        Mono<String> mono = this.requester
                .route("server.getEncryptMode")
                .data(serverID)
                .retrieveMono(String.class);
        return mono;
    }

    public Mono<byte []> getIV(UpdateRequest updateRequest){
        Mono<byte []> mono = this.requester
                .route("server.getIV")
                .data(updateRequest)
                .retrieveMono(byte [].class);
        return mono;
    }

    public Mono<User> signUp(User user) {
        return this.requester
                .route("user.signUp")
                .data(user)
                .retrieveMono(User.class);
    }

    public Flux<User> updateUsers(UpdateRequest updateRequest) {
        return this.requester
                .route("user.updateUsers")
                .data(updateRequest)
                .retrieveFlux(User.class);
    }

    public Mono<User> updateProfile(User user) {
        return this.requester
                .route("user.updateProfile")
                .data(user)
                .retrieveMono(User.class);
    }

    public Mono<Boolean> changeSecret(ChangeSecretRequest changeSecretRequest) {
        return this.requester
                .route("user.changeSecret")
                .data(changeSecretRequest)
                .retrieveMono(Boolean.class);
    }

    public String getLastDataTimeReg() {
        Timestamp timestamp = new Timestamp(0);
        for (User user : userRepository.findAll()) {
            if (user.getRegTime().after(timestamp))
                timestamp.setTime(user.getRegTime().getTime());
        }
        timestamp.setTime(timestamp.getTime() + 1);
        return timestamp.toString();
    }

    public Boolean changeSecretKey(String username) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        User user = userRepository.findByUsername(username).get();
        String newSecretKey = BigInteger.probablePrime(256, new SecureRandom()).toString();
        user.setSecret(newSecretKey);
        ChangeSecretRequest changeSecretRequest = new ChangeSecretRequest();
        changeSecretRequest.setServerID(ServerUtil.SERVER_ID);
        changeSecretRequest.setSecretKey(RSAUtil.encrypt(newSecretKey, ServerUtil.PUBLIC_KEY));
        changeSecretRequest.setUsername(username);
        changeSecretRequest.setRegTime(Timestamp.valueOf(LocalDateTime.now().format(formatter)));
        return changeSecret(changeSecretRequest).block();
    }

    public void updateEncryptMode() {
        threadUpdateEncryptMode = new Thread(() -> {
            ServerUtil.ENCRYPT_MODE = getEncryptMode(ServerUtil.SERVER_ID).block();
            logger.info(ServerUtil.SERVER_ID + " -> update encrypt mode: " + ServerUtil.ENCRYPT_MODE + " successfully!");
            do {
                try {
                    Thread.sleep(1000);
                    if(LocalDateTime.now().getSecond() == 0) {
                        ServerUtil.ENCRYPT_MODE = getEncryptMode(ServerUtil.SERVER_ID).block();
                        logger.info(ServerUtil.SERVER_ID + " -> update encrypt mode: " + ServerUtil.ENCRYPT_MODE + " successfully!");
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            } while (true);
        });
        threadUpdateEncryptMode.start();
    }

    public void updateIV() {
        threadUpdateIV = new Thread(() -> {
            UpdateRequest updateRequest = generateKeyPair();
            try {
                ServerUtil.IV_VALUE = RSAUtil.decrypt(getIV(updateRequest).block(), rsaUtil.getPrivateKey());
            } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                     NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            logger.info(ServerUtil.SERVER_ID + " -> update IV: " + Arrays.toString(ServerUtil.IV_VALUE) + " successfully!");
            do {
                try {
                    Thread.sleep(1000);
                    if(LocalDateTime.now().getSecond() == 0) {
                        try {
                            ServerUtil.IV_VALUE = RSAUtil.decrypt(getIV(updateRequest).block(), rsaUtil.getPrivateKey());
                        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                                 NoSuchAlgorithmException | InvalidKeyException e) {
                            throw new RuntimeException(e);
                        }
                        logger.info(ServerUtil.SERVER_ID + " -> update IV: " + Arrays.toString(ServerUtil.IV_VALUE) + " successfully!");
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            } while (true);
        });
        threadUpdateIV.start();
    }

    public void updateSalt() {
        threadUpdateSalt = new Thread(() -> {
            UpdateRequest updateRequest = generateKeyPair();
            try {
                ServerUtil.SALT_VALUE = RSAUtil.decrypt(getSalt(updateRequest).block(), rsaUtil.getPrivateKey());
            } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                     NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            logger.info(ServerUtil.SERVER_ID + " -> update salt: " + ServerUtil.SALT_VALUE + " successfully!");
            do {
                try {
                    Thread.sleep(1000);
                    if(LocalDateTime.now().getSecond() == 0) {
                        try {
                            ServerUtil.SALT_VALUE = RSAUtil.decrypt(getSalt(updateRequest).block(), rsaUtil.getPrivateKey());
                        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                                 NoSuchAlgorithmException | InvalidKeyException e) {
                            throw new RuntimeException(e);
                        }
                        logger.info(ServerUtil.SERVER_ID + " -> update salt: " + ServerUtil.SALT_VALUE + " successfully!");
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            } while (true);
        });
        threadUpdateSalt.start();
    }

    public void updateUsersTable() {
        threadUpdateUsers = new Thread(() -> {
            UpdateRequest updateRequest = generateKeyPair();
            do {
                try {
                    Thread.sleep(10000);
                    updateRequest.setRegTime(getLastDataTimeReg());
                    List<User> users = updateUsers(updateRequest).collectList().block();
                    for (User user : users) {
                        if (userRepository.findByUsername(user.getUsername()).isEmpty()) {
                            user.setSecret(RSAUtil.decrypt(user.getSecret(), rsaUtil.getPrivateKey()));
                            userRepository.save(user);
                        }
                    }
                    logger.info(ServerUtil.SERVER_ID + " -> update: " + users.size() + " user successfully!");
                } catch (Exception e) {
                    logger.error(e);
                }
            } while (true);
        });
        threadUpdateUsers.start();
    }
    private UpdateRequest generateKeyPair(){
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setServerId(ServerUtil.SERVER_ID);
        updateRequest.setPublicKey(Base64.getEncoder().encodeToString(rsaUtil.getPublicKey().getEncoded()));
        return updateRequest;
    }
    public Boolean validatePassword(SignUpRequest request) {
        if (request.getPassword().equals(request.getConfirmPassword())) {
            return true;
        }
        return false;
    }

    //todo потрібно зробити обробку винятку Duplicate entry 'exemple@gmail.com' for key 'users.users_email_uindex'
    public Boolean signUp(SignUpRequest request) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        User user = UserMapper.mapUserRequest(request);
        String secret = user.getSecret();
        user.setSecret(RSAUtil.encrypt(secret, ServerUtil.PUBLIC_KEY));
        user = signUp(user).block();
        if (user != null) {
            user.setSecret(secret);
            save(user);
            UserController.INFO = user.getFullName() + " is successfully registered!";
            logger.info(UserController.INFO);
            return true;
        } else {
            deleteByUsername(user.getUsername());
            UserController.INFO = "This user is duplicate entry email or username!";
            logger.info(UserController.INFO);
            return false;
        }
    }

    public Boolean updateProfile(SignUpRequest updateProfileRequest) {
        Optional<User> user = userRepository.findByUsername(updateProfileRequest.getUsername());
        if (user.isEmpty())
            return false;
        user.get().setFullName(updateProfileRequest.getFullName());
        user.get().setEmail(updateProfileRequest.getEmail());
        user.get().setPassword(BCrypt.withDefaults().hashToString(12, updateProfileRequest.getConfirmPassword().toCharArray()));
        user.get().setRegTime(Timestamp.valueOf(LocalDateTime.now().format(formatter)));
        updateProfile(user.get()).block();
        return true;
    }

    public Role isAdmin(String username) {
        return userRepository.isAdmin(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public String findSecretByUsername(String username) {
        return userRepository.findSecretByUsername(username);
    }

    List<String> findAllUsername() {
        return userRepository.findAllUsername();
    }

    public void deleteByUsername(String username) {
        userRepository.deleteByUsername(username);
    }
}


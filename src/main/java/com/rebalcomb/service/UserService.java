package com.rebalcomb.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rebalcomb.controllers.UserController;
import com.rebalcomb.mapper.UserMapper;
import com.rebalcomb.model.dto.KeyPair;
import com.rebalcomb.model.dto.SignUpRequest;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.repository.UserRepository;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private RSocketRequester requester;
    private final RSocketRequester.Builder builder;
    private final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository , RSocketRequester.Builder builder) {
        this.builder = builder;
        this.requester = this.builder
                        .transport(TcpClientTransport
                                .create("localhost", 7000));
        this.userRepository = userRepository;
        updateUsersTable();
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public Mono<KeyPair> getPublicKey(String serverId) {

        Mono<KeyPair> mono = this.requester
                .route("server.getPublicKey")
                .data(serverId)
                .retrieveMono(KeyPair.class);
        return mono;
    }

    public Mono<User> signUp(User user) {
        return this.requester
                .route("user.signUp")
                .data(user)
                .retrieveMono(User.class);
    }

    public Flux<User> updateUsers(String serverId) {
        return this.requester
                .route("user.updateUsers")
                .data(serverId)
                .retrieveFlux(User.class);
    }

        public Mono<User> searchUserInMainServer(String username) {
        return  this.requester
                .route("user.search")
                .data(username)
                .retrieveMono(User.class);
    }

    public void updateUsersTable(){
        Thread threadIncoming = new Thread(() -> {
            try {
                do {
                    List<User> users = Objects.requireNonNull(updateUsers("1").collectList().block());
                    for (User user : users) {
                        if(userRepository.findByUsername(user.getUsername()).isEmpty())
                            userRepository.save(user);
                    }
                    logger.info("Server: 1 -> update Users");
                    Thread.sleep(10000);
                }
                while (true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        threadIncoming.start();
    }

    public Boolean validatePassword(SignUpRequest request) {
        if (request.getPassword().equals(request.getConfirmPassword())) {
            return true;
        }
        return false;
    }

    //todo потрібно зробити обробку винятку Duplicate entry 'exemple@gmail.com' for key 'users.users_email_uindex'
    public Boolean signUp(SignUpRequest request) {
        User user = UserMapper.mapUserRequest(request);
        user = signUp(user).block();
        if(user != null) {
            userRepository.save(user);
            UserController.INFO = user.getFullName() + " is successfully registered!";
            return true;
        } else
            UserController.INFO = "This user already exists!";
        return false;
    }

    public Boolean updateProfile(SignUpRequest updateProfileRequest){
        Optional<User> user = userRepository.findByUsername(updateProfileRequest.getUsername());
        if(user.isEmpty())
            return false;
        user.get().setUsername(updateProfileRequest.getUsername());
        user.get().setFullName(updateProfileRequest.getFullName());
        user.get().setEmail(updateProfileRequest.getEmail());
        user.get().setPassword(BCrypt.withDefaults().hashToString(12, updateProfileRequest.getPassword().toCharArray()));
        userRepository.save(user.get());
        return true;
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public String findSecretByUsername(String username){
        return userRepository.findSecretByUsername(username);
    }

    List<String> findAllUsername(){
        return userRepository.findAllUsername();
    }

}


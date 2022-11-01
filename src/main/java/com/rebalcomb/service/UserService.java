package com.rebalcomb.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rebalcomb.controllers.UserController;
import com.rebalcomb.crypto.RSAUtil;
import com.rebalcomb.exceptions.DuplicateAccountException;
import com.rebalcomb.mapper.UserMapper;
import com.rebalcomb.model.dto.SignUpRequest;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RSocketService rSocketService;
    private final Logger logger = LogManager.getLogger(UserService.class);
    @Autowired
    public UserService(UserRepository userRepository, RSocketService rSocketService) {
        this.userRepository = userRepository;
        this.rSocketService = rSocketService;

    }

    public Boolean validatePassword(SignUpRequest request) {
        if (request.getPassword().equals(request.getConfirmPassword())) {
            return true;
        }
        return false;
    }

    //todo потрібно зробити обробку винятку Duplicate entry 'exemple@gmail.com' for key 'users.users_email_uindex'
    public Boolean signUp(SignUpRequest request) throws DuplicateAccountException {
        User user = UserMapper.mapUserRequest(request);
        try {
            user = rSocketService.sendUser(user);
        } catch (Exception e) {
            throw new DuplicateAccountException();
        }

        if(user != null) {
            userRepository.save(user);
            UserController.INFO = user.getFullName() + " is successfully registered!";
            return true;
        } else
            UserController.INFO = "This user already exists!";
        return false;
    }

    public void getPublicKeyFromMainServer() {
        RSAUtil.KEY_PAIR = rSocketService.getPublicKey("1");
        logger.info("Get public key: " +  RSAUtil.KEY_PAIR.getPublicKey() + " successfully!");
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

    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public String findSecretByUsername(String username){
        return userRepository.findSecretByUsername(username);
    }

    List<String> findAllUsername(){
        return userRepository.findAllUsername();
    }

}


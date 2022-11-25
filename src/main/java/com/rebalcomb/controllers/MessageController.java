package com.rebalcomb.controllers;

import com.rebalcomb.config.ServerUtil;
import com.rebalcomb.controllers.utils.Util;
import com.rebalcomb.model.dto.ConnectionRequest;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.dto.SettingRequest;
import com.rebalcomb.model.dto.SignUpRequest;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.service.LogService;
import com.rebalcomb.service.MessageService;
import com.rebalcomb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Flux;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


@Controller
@RequestMapping("/headPage")
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    private final UserService userService;
    private final LogService logService;
    private final Util util;

    @Autowired
    public MessageController(MessageService messageService, UserService userService, LogService logService, Util util) {
        this.messageService = messageService;
        this.userService = userService;
        this.logService = logService;
        this.util = util;
    }

    @GetMapping(value = "/findAll")
    public Flux<Message> findAll() {
        return messageService.findAll();
    }

    @GetMapping
    public ModelAndView headPage(ModelAndView model, Principal principal) {
        model.addObject("isAdmin", util.isAdmin(principal));
        model.addObject("headPageValue", "main");
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/write")
    public ModelAndView write(ModelAndView model, Principal principal) {
        model.addObject("headPageValue", "write");
        model.addObject("isAdmin", util.isAdmin(principal));
        model.addObject("messageRequest", new MessageRequest());
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/incoming")
    public ModelAndView incoming(ModelAndView model, Principal principal) throws IOException, InterruptedException {
        model.addObject("messages", messageService.findAllByRecipient(principal.getName()));
        model.addObject("isAdmin", util.isAdmin(principal));
        model.addObject("headPageValue", "incoming");
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/outcoming")
    public ModelAndView outcoming(ModelAndView model, Principal principal) throws IOException {
        model.addObject("messages", messageService.findAllBySender(principal.getName()));
        model.addObject("isAdmin", util.isAdmin(principal));
        model.addObject("headPageValue", "outcoming");
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.setViewName("headPage");
        return model;
    }

    private ModelAndView inputSetting(ModelAndView model, Principal principal) {
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.addObject("isAdmin", util.isAdmin(principal));
        model.addObject("headPageValue", "setting");
        model.addObject("connectionRequest", new ConnectionRequest());
        model.addObject("settingRequest", new SettingRequest());
        model.addObject("addressServer", ServerUtil.REMOTE_SERVER_IP_ADDRESS);
        model.addObject("portServer", ServerUtil.REMOTE_SERVER_PORT);
        model.addObject("serverId", ServerUtil.SERVER_ID);
        model.addObject("aesLen", ServerUtil.AES_LENGTH);
        model.addObject("rsaLen", ServerUtil.RSA_LENGTH);
        model.addObject("hash", ServerUtil.HASH_ALGORITHM);
        model.addObject("pool", ServerUtil.POOL_IMAGES_LENGTH);
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/setting")
    public ModelAndView setting(ModelAndView model, Principal principal) {
        return inputSetting(model, principal);
    }

    // todo пофіксити помилку якщо не коректні ip адреса і порт
    // todo ошибка port out of range:70000
    // todo Connection refused: no further information
    // todo Failed to resolve '2141241' after 3 queries
    // todo Connection timed out: no further information
    // todo Зробити вивід помилки на сторінку

    @PostMapping("/testConnection")
    public ModelAndView testConnection(ModelAndView model, ConnectionRequest connectionRequest, Principal principal) {
        if (!Pattern.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", connectionRequest.getIpAddress())
                || connectionRequest.getIpAddress().equals("localhost")) {
            return inputSetting(model, principal);
        }

        int port = 0;
        try {
            port = Integer.parseInt(connectionRequest.getPort());
        } catch (Exception e) {
            //не коректний порт
            return inputSetting(model, principal);
        }

        if (port <= 0 || port > 65000) {
            //не коректний порт
            return inputSetting(model, principal);
        }
        ServerUtil.REMOTE_SERVER_IP_ADDRESS = connectionRequest.getIpAddress();
        ServerUtil.REMOTE_SERVER_PORT = port;
        try {
            userService.isConnection().block();
        } catch (Exception e) {
            //час очікування перевищено
            return inputSetting(model, principal);
        }
        userService.requesterInitialization();
        messageService.requesterInitialization();
        ServerUtil.IS_CONNECTION = true;
        return inputSetting(model, principal);
    }

    // todo   ServerUtil.SERVER_ID = settingRequest.getServerID(); не може бути null
    // todo For input string: "" --------> ServerUtil.POOL_IMAGES_LENGTH = Integer.valueOf(settingRequest.getImagesPoolCount()); не може бути null
    @PostMapping("/applySetting")
    public ModelAndView applySetting(ModelAndView model, SettingRequest settingRequest, Principal principal) {
        if (!(settingRequest.getServerID().length() > 1) ) {
            //не коректний сервер id
            return inputSetting(model, principal);
        }

        int poolImages = 0;
        try {
            poolImages = Integer.parseInt(settingRequest.getImagesPoolCount());
        } catch (Exception e) {
            //не коректний пул картинок
            return inputSetting(model, principal);
        }

        if (poolImages < 1) {
            //не коректний пул картинок
            return inputSetting(model, principal);
        }
        ServerUtil.SERVER_ID = settingRequest.getServerID();
        ServerUtil.AES_LENGTH = Integer.valueOf(settingRequest.getAesLength());
        ServerUtil.RSA_LENGTH = Integer.valueOf(settingRequest.getRsaLength());
        ServerUtil.HASH_ALGORITHM = settingRequest.getHashType();
        ServerUtil.POOL_IMAGES_LENGTH = poolImages;
        return inputSetting(model, principal);
    }

    @GetMapping("/users")
    public ModelAndView users(ModelAndView model, Principal principal) {
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.addObject("headPageValue", "users");
        model.addObject("users", userService.findAll());
        model.addObject("isAdmin", util.isAdmin(principal));
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/logs")
    public ModelAndView logs(ModelAndView model, Principal principal) {
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.addObject("headPageValue", "logs");
        model.addObject("logs", logService.findAll());
        model.addObject("isAdmin", util.isAdmin(principal));
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/profile")
    public ModelAndView profile(ModelAndView model, Principal principal) {
        model.addObject("isOnline", ServerUtil.IS_CONNECTION);
        model.addObject("headPageValue", "profile");
        model.addObject("signUpRequest", getData(principal.getName()));
        model.addObject("isAdmin", util.isAdmin(principal));
        model.addObject("updateProfileRequest", new SignUpRequest());
        model.setViewName("headPage");
        return model;
    }

    public SignUpRequest getData(String username) {
        Optional<User> user = userService.findByUsername(username);
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(user.get().getUsername());
        signUpRequest.setEmail(user.get().getEmail());
        signUpRequest.setFullName(user.get().getFullName());
        return signUpRequest;
    }

    @GetMapping("/changeSecretKey")
    public ModelAndView changeSecretKey(ModelAndView model, Principal principal) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (userService.changeSecretKey(principal.getName())) {
            model.addObject("info", "Secret key changed successfully!");
        } else
            model.addObject("info", "Secret key not changed!");
        model.addObject("signUpRequest", getData(principal.getName()));
        model.addObject("key", userService.findSecretByUsername(principal.getName()));
        model.addObject("updateProfileRequest", new SignUpRequest());
        model.addObject("headPageValue", "profile");
        model.setViewName("headPage");
        return model;
    }
}

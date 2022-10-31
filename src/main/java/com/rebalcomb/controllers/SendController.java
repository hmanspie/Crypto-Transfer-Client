package com.rebalcomb.controllers;

import com.rebalcomb.crypto.RSAUtil;
import com.rebalcomb.mapper.BlockMapper;
import com.rebalcomb.mapper.MessageRequestMapper;
import com.rebalcomb.model.dto.SecretBlock;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.service.MessageService;
import com.rebalcomb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.ExecutionException;

@Controller
public class SendController {
    private final Logger logger = LoggerFactory.getLogger(SendController.class);
    private final MessageService messageService;
    private final UserService userService;

    public static String INFO;

    @Autowired
    public SendController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping("/sendNewMessage")
    public ModelAndView login(ModelAndView model, @Valid @ModelAttribute MessageRequest messageRequest, Principal principal) throws InterruptedException, IOException, ExecutionException {
        if(RSAUtil.KEY_PAIR == null)
            userService.getPublicKeyFromMainServer();
        if(messageRequest.getTo().equals(principal.getName())){
            model.addObject("isSend", false);
            model.addObject("headPageValue", "write");
            model.addObject("messageRequest", new MessageRequest());
            model.setViewName("headPage");
            logger.error("Address not found!");
            return model;
        }
        SecretBlock secretBlock =
                BlockMapper.mapBlockRequest(MessageRequestMapper.
                        mapMessageRequest(messageRequest, principal.getName()),
                                userService.findSecretByUsername(principal.getName()));

        if(messageService.sendMessage(secretBlock)){
            Message message = messageService.findTopByOrderByIdDesc();
            message.setIs_send(true);
            messageService.save(message);
            model.addObject("isSend", true);
            model.addObject("messages", messageService.findAllBySender(principal.getName()));
            model.addObject("headPageValue", "outcoming");
            model.setViewName("headPage");
            logger.info("Message sent successfully!");
            return model;
        }else{
            model.addObject("isSend", false);
            model.addObject("headPageValue", "write");
            model.addObject("messageRequest", new MessageRequest());
            model.setViewName("headPage");
            logger.error("Address not found!");
            return model;
        }
    }
}

package com.rebalcomb.controllers;

import com.rebalcomb.crypto.AESUtil;
import com.rebalcomb.crypto.rsa.RSAUtil;
import com.rebalcomb.model.dto.BlockRequest;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

@Controller
public class SendController {
    private Logger logger = LoggerFactory.getLogger(SendController.class);
    private final MessageService messageService;

    @Autowired
    public SendController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/sendNewMessage")
    public ModelAndView login(ModelAndView model, @Valid @ModelAttribute MessageRequest messageRequest) throws InterruptedException, IOException, ExecutionException {
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss");
        messageRequest.setDateTime(LocalDateTime.now().format(formatter));
        BlockRequest blockRequest = new BlockRequest(messageRequest, RSAUtil.encrypt(AESUtil.SECRET_KEY,
                                                                                        AccountController.KEY_PAIR.getPublicKey(),
                                                                                            AccountController.KEY_PAIR.getModule()));
        if(messageService.sendMessage(blockRequest)){
            model.addObject("messages", messageService.findAllBySender());
            model.addObject("headPageValue", "outcoming");
            model.setViewName("headPage");
            logger.info("Message sent successfully!");
            return model;
        }else{
            model.addObject("headPageValue", "write");
            model.addObject("messageRequest", new MessageRequest());
            model.setViewName("headPage");
            logger.error("Message not send!");
            return model;
        }
    }
}

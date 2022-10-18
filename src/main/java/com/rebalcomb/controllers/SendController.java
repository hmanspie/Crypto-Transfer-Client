package com.rebalcomb.controllers;

import com.rebalcomb.model.dto.NewMessageRequest;
import com.rebalcomb.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class SendController {
    private Logger logger = LoggerFactory.getLogger(SendController.class);
    private final MessageService messageService;

    @Autowired
    public SendController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/sendNewMessage")
    public ModelAndView login(ModelAndView model, @Valid @ModelAttribute NewMessageRequest newMessageRequest) throws InterruptedException {
        if(messageService.sendMessage(newMessageRequest)){
            model.addObject("messages", messageService.findAllBySender(AccountController.activeAccount));
            model.addObject("headPageValue", "outcoming");
            model.setViewName("headPage");
            logger.info("Message sent successfully!");
            return model;
        }else{
            model.addObject("headPageValue", "write");
            model.addObject("newMessageRequest", new NewMessageRequest());
            model.setViewName("headPage");
            logger.error("Message not sent!");
            return model;
        }
    }
}

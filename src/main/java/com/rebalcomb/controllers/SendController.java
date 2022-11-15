package com.rebalcomb.controllers;

import com.rebalcomb.mapper.MessageRequestMapper;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.model.entity.enums.TypeLog;
import com.rebalcomb.service.LogService;
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
    private final LogService logService;

    public static String INFO;

    @Autowired
    public SendController(MessageService messageService, UserService userService, LogService logService) {
        this.messageService = messageService;
        this.userService = userService;
        this.logService = logService;
    }

    @PostMapping("/sendNewMessage")
    public ModelAndView login(ModelAndView model, @Valid @ModelAttribute MessageRequest messageRequest, Principal principal) throws InterruptedException, IOException, ExecutionException {
        if(messageRequest.getUser_to().equals(principal.getName())){
            model.addObject("isSend", false);
            model.addObject("headPageValue", "write");
            model.addObject("messageRequest", new MessageRequest());
            model.setViewName("headPage");
            logger.error("Address not found!");
            return model;
        }
        MessageRequest messageRequestNew = MessageRequestMapper.mapMessageRequest(messageRequest, principal.getName());
        if(messageService.sendMessage(messageRequestNew)){
            Message message = messageService.findTopByOrderByIdDesc();
            message.setIs_send(true);
            messageService.save(message);
            model.addObject("isSend", true);
            model.addObject("messages", messageService.findAllBySender(principal.getName()));
            model.addObject("headPageValue", "outcoming");
            model.setViewName("headPage");
            logger.info("Message sent successfully!");
            logService.create(TypeLog.MESSAGE, "Write message TO: '" + messageRequest.getUser_to()+ "' | FROM: '" + messageRequest.getUser_from() + "' | TITLE: '" + messageRequest.getTitle());
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

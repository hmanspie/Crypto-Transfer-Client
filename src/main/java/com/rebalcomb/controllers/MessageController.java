package com.rebalcomb.controllers;

import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.entity.Message;
import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.service.MessageService;
import com.rebalcomb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Flux;
import java.io.IOException;
import java.security.Principal;


@Controller
@RequestMapping("/headPage")
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    private  final UserService userService;
    @Autowired
    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }
    @GetMapping(value = "/findAll")
    public Flux<Message> findAll() {
        return messageService.findAll();
    }

    @GetMapping
    public ModelAndView headPage(ModelAndView model, Principal principal){
        if(userService.isAdmin(principal.getName()).equals(Role.USER)){
            model.addObject("headPageValue", "main");
            model.setViewName("headPage");
        }else {
            model.addObject("headPageValue", "admin");
            model.setViewName("adminPanel");
        } return model;
    }

    @GetMapping("/home")
    public ModelAndView home(ModelAndView model){
        model.addObject("headPageValue", "home");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/write")
    public ModelAndView write(ModelAndView model){
        model.addObject("headPageValue", "write");
        model.addObject("messageRequest", new MessageRequest());
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/incoming")
    public ModelAndView incoming(ModelAndView model, Principal principal) throws IOException, InterruptedException {
        model.addObject("messages",messageService.findAllByRecipient(principal.getName()));
        model.addObject("headPageValue", "incoming");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/outcoming")
    public ModelAndView outcoming(ModelAndView model, Principal principal) throws IOException {
        model.addObject("messages", messageService.findAllBySender(principal.getName()));
        model.addObject("headPageValue", "outcoming");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/message/1")
    public ModelAndView message(ModelAndView model){
        model.addObject("headPageValue", "messageShow");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/setting")
    public ModelAndView settings(ModelAndView model){
        model.addObject("headPageValue", "setting");
        model.setViewName("headPage");
        return model;
    }
}

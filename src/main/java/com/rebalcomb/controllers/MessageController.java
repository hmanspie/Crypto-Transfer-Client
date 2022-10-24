package com.rebalcomb.controllers;

import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/headPage")
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ModelAndView headPage(ModelAndView model){
        model.addObject("headPageValue", "main");
        model.setViewName("headPage");
        return model;
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
    public ModelAndView incoming(ModelAndView model) throws IOException, InterruptedException {
        model.addObject("messages",messageService.findAllByRecipient());
        model.addObject("headPageValue", "incoming");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/outcoming")
    public ModelAndView outcoming(ModelAndView model) throws IOException, ExecutionException, InterruptedException {
        model.addObject("messages", messageService.findAllBySender());
        model.addObject("headPageValue", "outcoming");
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

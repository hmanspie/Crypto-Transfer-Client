package com.rebalcomb.controllers;

import com.rebalcomb.model.entity.Message;
import com.rebalcomb.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;
@Controller
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/headPage")
    public ModelAndView headPage(ModelAndView model){
        model.addObject("headPageValue", "main");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/headPage/write")
    public ModelAndView write(ModelAndView model){
        model.addObject("headPageValue", "write");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/headPage/incoming")
    public ModelAndView incoming(ModelAndView model) throws InterruptedException {
        model.addObject("messages",messageService.findAllByRecipient(AccountController.activeAccount));
        model.addObject("headPageValue", "incoming");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/headPage/outcoming")
    public ModelAndView outcoming(ModelAndView model) throws InterruptedException {
        model.addObject("messages", messageService.findAllBySender(AccountController.activeAccount));
        model.addObject("headPageValue", "outcoming");
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/headPage/setting")
    public ModelAndView settings(ModelAndView model){
        model.addObject("headPageValue", "setting");
        model.setViewName("headPage");
        return model;
    }
}

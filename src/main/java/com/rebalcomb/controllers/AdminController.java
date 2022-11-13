package com.rebalcomb.controllers;

import com.rebalcomb.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final LogService logService;
    @Autowired
    public AdminController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/logs")
    public ModelAndView getLogs(ModelAndView modelAndView){
        modelAndView.addObject("adminPanelValue", "logs");
        modelAndView.addObject("logs", logService.findAll());
        modelAndView.setViewName("adminPanel");

        return modelAndView;
    }

    @GetMapping("/setting")
    public ModelAndView getSetting(ModelAndView modelAndView){
        modelAndView.addObject("adminPanelValue", "setting");
        modelAndView.setViewName("adminPanel");

        return modelAndView;
    }
}
package com.rebalcomb.controllers;

import com.rebalcomb.controllers.utils.Util;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.model.entity.enums.Status;
import com.rebalcomb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;

@Controller
public class UsersController {

    private Logger logger = LoggerFactory.getLogger(UsersController.class);
    private final UserService userService;
    private final Util util;

    @Autowired
    public UsersController(UserService userService, Util util) {
        this.userService = userService;
        this.util = util;
    }

    @PostMapping("/users/edit/{id}")
    public ModelAndView editUser(@PathVariable Long id, @Valid String role, @Valid String userStatus, ModelAndView model, Principal principal) {
        User user = userService.findById(id).orElse(null);

        if (user != null) {
            user.setRole(Role.valueOf(role));
            user.setStatus(Status.valueOf(userStatus));
            userService.save(user);
        }

        model.addObject("headPageValue", "users");
        model.addObject("users", userService.findAll());
        model.addObject("isAdmin", util.isAdmin(principal));
        model.setViewName("headPage");
        return model;
    }

    @GetMapping("/users/delete/{id}")
    public ModelAndView deleteUser(@PathVariable Long id, ModelAndView model, Principal principal) {
        userService.deleteById(id);
        model.addObject("headPageValue", "users");
        model.addObject("users", userService.findAll());
        model.addObject("isAdmin", util.isAdmin(principal));
        model.setViewName("headPage");
        return model;
    }

}

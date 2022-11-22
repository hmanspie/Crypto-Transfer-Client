package com.rebalcomb.controllers.utils;

import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class Util {

    private final UserService userService;

    @Autowired
    public Util(UserService userService) {
        this.userService = userService;
    }

    /**
     * Check admin role in current user
     * @param principal
     * @return current user is admin
     */
    public boolean isAdmin(Principal principal){
        return userService.isAdmin(principal.getName()).equals(Role.ADMIN);
    }

}

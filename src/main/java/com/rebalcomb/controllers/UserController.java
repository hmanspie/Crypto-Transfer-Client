package com.rebalcomb.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rebalcomb.exceptions.DuplicateAccountException;
import com.rebalcomb.model.dto.SignInRequest;
import com.rebalcomb.model.dto.SignUpRequest;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Controller
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    public static String INFO;
    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @RequestMapping(value = {"/","/login"}, method = RequestMethod.GET)
    public String getLoginPage() {
        return "login";
    }

    @PostMapping("/registered")
    public ModelAndView registered(@Valid @ModelAttribute SignUpRequest signUpRequest,
                                                        ModelAndView model) throws InterruptedException, ExecutionException, DuplicateAccountException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        model.setViewName("login");
        if (!userService.validatePassword(signUpRequest)) {
            model.addObject("isError", true);
            model.addObject("error", "Confirm password doesn't match!");
            model.addObject("howForm", true);
            return model;
        }
        if (userService.signUp(signUpRequest)) {
            model.addObject("isError", false);
            model.addObject("info", INFO);
            model.addObject("signInRequest", new SignInRequest());
            model.addObject("howForm", false);
        } else{
            model.addObject("isError", true);
            model.addObject("error", INFO);
            model.addObject("howForm", true);
        }
        return model;
    }

    @GetMapping("/goToSignUpForm")
    public ModelAndView goToSignUpForm(ModelAndView model) {
        model.addObject("signUpRequest", new SignUpRequest());
        model.addObject("howForm", true);
        model.setViewName("login");
        return model;
    }

    @GetMapping("/goToSignInForm")
    public ModelAndView goToSignInForm(ModelAndView model) {
        model.addObject("accountSignInRequest", new SignInRequest());
        model.addObject("howForm", false);
        model.setViewName("login");
        return model;
    }

    @GetMapping("/goToForgortPasswordForm")
    public ModelAndView getViewForForgortPassword(ModelAndView model) {
        model.setViewName("email");
        return null;
    }

    @GetMapping("/email")
    public String email(){
        return "email";
    }

    @GetMapping("/logout")
    public String logout(){
        return "logout";
    }

    @GetMapping("/headPage/profile")
    public ModelAndView profile(ModelAndView model, Principal principal){
        Optional<User> user = userService.findByUsername(principal.getName());
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(user.get().getUsername());
        signUpRequest.setEmail(user.get().getEmail());
        signUpRequest.setFullName(user.get().getFullName());
        model.addObject("headPageValue", "profile");
        model.addObject("signUpRequest", signUpRequest);
        model.addObject("updateProfileRequest", new SignUpRequest());
        model.setViewName("headPage");
        return model;
    }

    @PostMapping("/updateProfile")
    public ModelAndView updateProfile(@Valid @ModelAttribute SignUpRequest updateProfileRequest, ModelAndView model, Principal principal) {

        if (!userService.validatePassword(updateProfileRequest)) {
            model.addObject("error", "Confirm password doesn't match!");
            model.addObject("headPageValue", "profile");
            model.addObject("updateProfileRequest", new SignUpRequest());
            model.setViewName("headPage");
            return model;
        }
        if (userService.updateProfile(updateProfileRequest)) {
            model.addObject("isError", false);
            model.addObject("info", INFO);
            model.addObject("headPageValue", "profile");
            model.addObject("updateProfileRequest", new SignUpRequest());
            model.setViewName("headPage");
        } else{
            model.addObject("isError", true);
            model.addObject("error", INFO);
        }
        return model;
    }
}

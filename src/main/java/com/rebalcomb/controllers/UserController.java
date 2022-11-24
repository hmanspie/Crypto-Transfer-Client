package com.rebalcomb.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rebalcomb.email.EmailHandler;
import com.rebalcomb.exceptions.DuplicateAccountException;
import com.rebalcomb.model.dto.*;
import com.rebalcomb.model.entity.Log;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.model.entity.enums.TypeLog;
import com.rebalcomb.service.LogService;
import com.rebalcomb.service.UserService;
import jdk.jfr.Timespan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Controller
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final LogService logService;
    public static String INFO;
    public SignUpRequest signUpRequest;

    @Autowired
    public UserController(UserService userService, LogService logService) {
        this.userService = userService;
        this.logService = logService;
    }

    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    public String getLoginPage() {
        return "login";
    }

    @PostMapping("/registered")
    public ModelAndView registered(@Valid @ModelAttribute SignUpRequest signUpRequest,
                                   ModelAndView model) throws InterruptedException, ExecutionException, DuplicateAccountException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (!userService.validatePassword(signUpRequest)) {
            model.setViewName("login");
            model.addObject("isError", true);
            model.addObject("error", "Confirm password doesn't match!");
            model.addObject("howForm", true);
            return model;
        }
        if (userService.signUp(signUpRequest)) {
            Thread thread = new Thread(() -> {
                EmailHandler email = new EmailHandler();
                email.send(EmailRequest.checkEmail);
            });
            thread.start();
            model.setViewName("email");
            model.addObject("checkCode", new CheckCode());
            model.addObject("messageForUser", "");
            model.addObject("headPageValue", "registration");
        } else {
            model.setViewName("login");
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
        model.setViewName("enterEmail");
        model.addObject("emailRequest", new EmailRequest());
        return model;
    }

    @PostMapping("/checkUsersEmail")
    public ModelAndView checkUsersEmail(@Valid @ModelAttribute SignUpRequest signUpRequest, ModelAndView model) {
        this.signUpRequest = signUpRequest;
        new Thread(() -> {
            new EmailHandler().send(signUpRequest.getEmail());
        }).start();
        model.setViewName("email");
        model.addObject("checkCode", new CheckCode());
        model.addObject("messageForUser", "");
        model.addObject("headPageValue", "registration");
        return model;
    }

    @PostMapping("/sendCode")
    public ModelAndView sendCode(ModelAndView model, EmailRequest request) {
        EmailRequest.checkEmail = request.getEmail();
        Thread thread = new Thread(() -> {
            EmailHandler email = new EmailHandler();
            email.send(EmailRequest.checkEmail);
        });
        thread.start();
        model.setViewName("email");
        model.addObject("checkCode", new CheckCode());
        model.addObject("messageForUser", "");
        model.addObject("headPageValue", "forgotPassword");
        return model;
    }

    @PostMapping("/verificatedAccount")
    public ModelAndView verificatedAccount(ModelAndView model, CheckCode code) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, DuplicateAccountException {
        if (code.getCode().equals(EmailHandler.verificationCode)) {

            if (!userService.validatePassword(signUpRequest)) {
                model.setViewName("login");
                model.addObject("isError", true);
                model.addObject("error", "Confirm password doesn't match!");
                model.addObject("howForm", true);
                return model;
            }
            try {
                userService.signUp(signUpRequest);
                model.setViewName("/login");
            } catch (Exception e) {
                logger.error("Already existing user");
                logService.save(new Log(0L, TypeLog.SIGN_UP, Timestamp.valueOf(LocalDateTime.now()), "Already existing user"));
                throw new DuplicateAccountException();
            }
                model.setViewName("login");
                model.addObject("isError", true);
                model.addObject("error", INFO);
                model.addObject("howForm", true);

        } else {
            model.setViewName("email");
            model.addObject("checkCode", new CheckCode());
            model.addObject("messageForUser", "Invalid verification code");
            model.addObject("headPageValue", "registration");
        }
        return model;
    }

    @PostMapping("/createNewPassword")
    public ModelAndView createNewPassword(ModelAndView model, CheckCode code) {
        if (code.getCode().equals(EmailHandler.verificationCode)) {
            model.setViewName("recoveryPassword");
            model.addObject("updatePassword", new UpdatePassword());
            model.addObject("updatePassword", new UpdatePassword());
            model.addObject("messageForUser", "");
        } else {
            model.setViewName("email");
            model.addObject("checkCode", new CheckCode());
            model.addObject("messageForUser", "Invalid verification code");
        }
        return model;
    }

    @PostMapping("/updatePassword")
    public ModelAndView updatePasswordInDataBase(ModelAndView model, UpdatePassword password) {
        if (password.getPassword().equals(password.getConfirmationPassword())) {
            String email = EmailRequest.checkEmail;
            model.setViewName("login");
            User user = userService.findByEmail(email).get();
            user.setPassword(BCrypt.withDefaults().hashToString(12, password.getPassword().toCharArray()));
            userService.save(user);
            userService.updateProfile(user);
        } else {
            model.setViewName("recoveryPassword");
            model.addObject("updatePassword", new UpdatePassword());
            model.addObject("updatePassword", new UpdatePassword());
            model.addObject("messageForUser", "Passwords do not match");
        }
        return model;
    }

    @GetMapping("/email")
    public String email() {
        return "email";
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    @PostMapping("/updateProfile")
    public ModelAndView updateProfile(@Valid @ModelAttribute SignUpRequest updateProfileRequest,
                                      ModelAndView model) {
        if (!userService.validatePassword(updateProfileRequest)) {
            model.addObject("error", "Confirm password doesn't match!");
            model.addObject("headPageValue", "profile");
            model.setViewName("headPage");
            return model;
        }
        if (userService.updateProfile(updateProfileRequest)) {
            model.addObject("isError", false);
            model.addObject("info", INFO);
            model.addObject("signUnRequest", updateProfileRequest);
        } else {
            model.addObject("isError", true);
            model.addObject("error", INFO);
        }
        return model;
    }
}

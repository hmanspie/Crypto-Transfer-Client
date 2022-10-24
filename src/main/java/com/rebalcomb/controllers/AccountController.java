package com.rebalcomb.controllers;

import com.rebalcomb.model.dto.AccountSignInRequest;
import com.rebalcomb.model.dto.AccountSignUpRequest;
import com.rebalcomb.model.dto.KeyPairRequest;
import com.rebalcomb.model.entity.Account;
import com.rebalcomb.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class AccountController {

    public static Account activeAccount;
    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    public static KeyPairRequest KEY_PAIR;
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @RequestMapping(value = {"/","/login.html"}, method = RequestMethod.GET)
    public ModelAndView index(ModelAndView model) {
        model.setViewName("login");
        model.addObject("accountSignInRequest", new AccountSignInRequest());
        return model;
    }

    @PostMapping("/login")
    public String login(Model model, @Valid @ModelAttribute AccountSignInRequest accountSignInRequest) throws InterruptedException {
        if (accountService.isAccess(accountSignInRequest)) {
            //activeAccount = accountSignInRequest.getLogin();
            accountService.getPublicKey();
            return "headPage";
        } else{
            logger.error("Incorrect login or password");
            model.addAttribute("isError", true);
            model.addAttribute("error", "Incorrect login or password!");
            AccountController.activeAccount = null;
            return "login";
        }
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid @ModelAttribute AccountSignUpRequest accountSignUpRequest,
                                                        ModelAndView model) throws InterruptedException {
        model.setViewName("login");
        if (!accountService.validatePassword(accountSignUpRequest)) {
            model.addObject("isError", true);
            model.addObject("error", "Confirm password doesn't match!");
            model.addObject("howForm", true);
            return model;
        }
        if (accountService.saveUser(accountSignUpRequest)) {
            model.addObject("isError", false);
            model.addObject("info", "Account successfully registered!");
            model.addObject("accountSignInRequest", new AccountSignInRequest());
            model.addObject("howForm", false);
        } else{
            model.addObject("isError", true);
            model.addObject("error", "No connection to the server!");
            model.addObject("howForm", true);
        }
        return model;
    }

    @GetMapping("/goToSignUpForm")
    public ModelAndView goToSignUpForm(ModelAndView model) {
        model.addObject("accountSignUpRequest", new AccountSignUpRequest());
        model.addObject("howForm", true);
        model.setViewName("login");
        return model;
    }

    @GetMapping("/goToSignInForm")
    public ModelAndView goToSignInForm(ModelAndView model) {
        model.addObject("accountSignInRequest", new AccountSignInRequest());
        model.addObject("howForm", false);
        model.setViewName("login");
        return model;
    }

    @GetMapping("/goToForgortPasswordForm")
    public String getViewForForgortPassword(Model model) {
        return "forgort";
    }

    @GetMapping("/email")
    public String email(){
        return "email";
    }
}

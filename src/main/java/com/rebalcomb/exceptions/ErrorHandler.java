package com.rebalcomb.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(DuplicateAccountException.class)
    public ModelAndView duplicateAccountProcessor(){
        String messageException = "This account is already registered";
        String pageException = "login";
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(pageException);
        modelAndView.addObject("isError", true);
        modelAndView.addObject("error", messageException);
        return modelAndView;
    }

    @ExceptionHandler(AuthenticationCodeException.class)
    public ModelAndView authenticationCodeProcessor() {
        String messageException = "Invalid code";
        String pageException = "";
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(pageException);
        return modelAndView;
    }

    @ExceptionHandler(ServerUnavailableException.class)
    public ModelAndView serverUnavailableProcessor() {
        String messageException = "The server is unavailable";
        String pageException = "";
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(pageException);
        return modelAndView;
    }

    @ExceptionHandler(SearchAddresseeException.class)
    public ModelAndView searchAddresseeProcessor() {
        String messageException = "Addressee not found";
        String pageException = "";
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(pageException);
        return modelAndView;
    }

    @ExceptionHandler(FoundUserException.class)
    public ModelAndView foundUserProcessor() {
        String messageException = "Not found account!";
        String pageException = "login";
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(pageException);
        modelAndView.addObject("isError", true);
        modelAndView.addObject("error", messageException);
        return modelAndView;
    }
}

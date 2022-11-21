package com.rebalcomb.email;

import java.security.SecureRandom;

public class EmailHandler {
    //TODO: Треба первіряти verificationCode з тим, що укаже користувач
    public static String verificationCode;

    public boolean isVereficated(String userCode)
    {
        //boolean verificated = false;
        return userCode.equals(verificationCode);
    }

    public void send(String email) {
        SecureRandom random = new SecureRandom();
        String code = String.valueOf(random.nextInt(999999));
        verificationCode = code;

        TLSEmail tlsEmail = new TLSEmail();
        tlsEmail.answerToEmail(email, code);
    }
}
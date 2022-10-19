package com.rebalcomb.email;

import java.security.SecureRandom;

public class EmailHandler {
    //TODO: Треба первіряти verificationCode з тим, що укаже користувач
    private static String verificationCode;

    /**
     * @
     * @param email
     * @return vereficated
     */
    public void isVereficated(String email)
    {
        boolean verificated = false;
        SecureRandom random = new SecureRandom();
        String code = String.valueOf(random.nextInt(999999));
        this.verificationCode = code;

        TLSEmail tlsEmail = new TLSEmail();
        tlsEmail.answerToEmail(email, code);
    }

}

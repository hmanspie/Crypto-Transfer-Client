package com.rebalcomb.email;

import java.security.SecureRandom;
import java.util.HashMap;

public class EmailHandler {
    public static HashMap<String, String> verificationCode = new HashMap<>();

    public boolean isVereficated(String userCode)
    {
        String code = verificationCode.get(userCode);
        if (code == null) {
            return false;
        }
        verificationCode.remove(userCode);
        return true;
    }

    public void send(String email) {
        SecureRandom random = new SecureRandom();
        String code = String.valueOf(random.nextInt(999999));
        verificationCode.put(email, code);

        TLSEmail tlsEmail = new TLSEmail();
        tlsEmail.answerToEmail(code, email);
    }
}
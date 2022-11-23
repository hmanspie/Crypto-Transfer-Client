package com.rebalcomb.crypto;

import com.rebalcomb.config.ServerUtil;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    public static String getHash(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance(ServerUtil.HASH_ALGORITHM);
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

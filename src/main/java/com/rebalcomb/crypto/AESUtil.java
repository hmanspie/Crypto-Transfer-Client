package com.rebalcomb.crypto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebalcomb.config.ServerUtil;
import com.rebalcomb.model.dto.MessageRequest;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESUtil {
    //private final IvParameterSpec ivspec = new IvParameterSpec(ServerUtil.IV_VALUE);

    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson = null;
    public static String encrypt(MessageRequest messageRequest, String SECRET_KEY) {
        gson = gsonBuilder.setPrettyPrinting().create();
        String strToEncrypt = gson.toJson(messageRequest);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), ServerUtil.SALT_VALUE.getBytes(), 65536, ServerUtil.AES_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/"+ ServerUtil.ENCRYPT_MODE +"/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(ServerUtil.IV_VALUE));
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            System.out.println("Error occured during encryption: " + e.toString());
        }
        return null;
    }

    public static MessageRequest decrypt(String strToDecrypt, String SECRET_KEY) {
        try {
            gson = gsonBuilder.setPrettyPrinting().create();
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), ServerUtil.SALT_VALUE.getBytes(), 65536, ServerUtil.AES_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/"+ ServerUtil.ENCRYPT_MODE +"/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ServerUtil.IV_VALUE));
            return gson.fromJson(new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt))), MessageRequest.class);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            System.out.println("Error occured during decryption: " + e.toString());
        }
        return null;
    }
}

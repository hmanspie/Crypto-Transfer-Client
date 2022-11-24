package com.rebalcomb.config;

import java.security.PublicKey;

public class ServerUtil {

    public static PublicKey PUBLIC_KEY;
    public static String STR_PUBLIC_KEY;
    public static byte [] IV_VALUE;
    public static String SALT_VALUE;
    public static String ENCRYPT_MODE = "CBC";
    public static String SERVER_ID = "Local server 101";
    public static String REMOTE_SERVER_IP_ADDRESS = "localhost";
    public static Integer REMOTE_SERVER_PORT = 7000;
    public static Boolean IS_CONNECTION = false;
    public static String HASH_ALGORITHM = "SHA-512";

    public static String ENCRYPT_ALGORITHM = "AES";
    public static Integer AES_LENGTH = 256;
    public static Integer RSA_LENGTH = 1024;
    public static Integer POOL_IMAGES_LENGTH = 5;



}

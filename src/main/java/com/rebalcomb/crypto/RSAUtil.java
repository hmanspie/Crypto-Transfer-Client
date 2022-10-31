package com.rebalcomb.crypto;

import com.rebalcomb.model.dto.KeyPair;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.security.SecureRandom;

@Setter
@Getter
@EqualsAndHashCode
public class RSAUtil {
    public static KeyPair KEY_PAIR;
    private final static BigInteger bitCount = new BigInteger("64");
    private BigInteger p;
    private BigInteger q;
    private BigInteger module;
    private BigInteger eulerNumber;
    private BigInteger publicKey;
    private BigInteger privateKey;

    public RSAUtil(){
        p = getRandomPrimaryNumber();
        q = getRandomPrimaryNumber();
        module = getCulcModule();
        eulerNumber = getEulerNumber();
        publicKey = genPublicKey();
        privateKey = genPrivateKey();
    }

    private BigInteger getRandomPrimaryNumber() {
        return BigInteger.probablePrime(bitCount.intValue(), new SecureRandom());
    }

    private BigInteger getCulcModule() {
        return p.multiply(q);
    }

    private BigInteger getEulerNumber() {
        BigInteger bigInteger = BigInteger.ONE;
        bigInteger = bigInteger.multiply(p.subtract(BigInteger.ONE));
        bigInteger = bigInteger.multiply(q.subtract(BigInteger.ONE));
        return bigInteger;
    }

    private BigInteger genPublicKey() {
        BigInteger exponent;
        while (true) {
            exponent = BigInteger.probablePrime(bitCount.intValue(),  new SecureRandom());
            if (exponent.gcd(eulerNumber).equals(BigInteger.ONE))
                break;
        }
        return exponent;
    }
    private BigInteger genPrivateKey() {
        return publicKey.modInverse(eulerNumber);
    }

    public static String encrypt(String value, BigInteger publicKey, BigInteger module) {
        BigInteger M = new BigInteger(value);
        return M.modPow(publicKey, module).toString();
    }

    public static String decrypt(String value, BigInteger privateKey, BigInteger module) {
        BigInteger C = new BigInteger(value);
        return C.modPow(privateKey, module).toString();
    }
}

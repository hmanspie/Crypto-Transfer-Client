package com.rebalcomb.mapper;

import com.rebalcomb.model.dto.SecretBlock;
import com.rebalcomb.model.dto.MessageRequest;
import org.springframework.stereotype.Component;

@Component
public class BlockMapper {

    public static SecretBlock mapBlockRequest(MessageRequest request, String secret) {
        SecretBlock secretBlock = new SecretBlock();
        secretBlock.setMessageRequest(request);
//        secretBlock.setSecretKey(RSAUtil.encrypt(secret,
//                                        RSAUtil.KEY_PAIR.getPublicKey(),
//                                                RSAUtil.KEY_PAIR.getModule()));
        secretBlock.setSecretKey(secret);
        return secretBlock;
    }
}

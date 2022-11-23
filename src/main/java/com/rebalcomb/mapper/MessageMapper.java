package com.rebalcomb.mapper;

import com.rebalcomb.config.ServerUtil;
import com.rebalcomb.model.dto.MessageRequest;
import com.rebalcomb.model.entity.Certificate;
import com.rebalcomb.model.entity.Message;
import java.sql.Timestamp;

public class MessageMapper {

    public static Message mapMessage(MessageRequest request, String hash) {
        Message message = new Message();
        message.setUser_from(request.getUser_from());
        message.setUser_to(request.getUser_to());
        message.setTitle(request.getTitle());
        message.setBody(request.getBodyMessage());
        message.setDate_time(Timestamp.valueOf(request.getDateTime()));
        message.setIs_send(false);
        message.setHash(hash);
        return message;
    }

    public static Certificate mapCertifacate(MessageRequest request, String secretKey){
        Certificate certificate = new Certificate();
        certificate.setOwner(request.getUser_from());
        certificate.setPublicKey(ServerUtil.STR_PUBLIC_KEY);
        certificate.setKeyLength(ServerUtil.RSA_LENGTH);
        certificate.setAlgorithm("AES-" + ServerUtil.AES_LENGTH);
        certificate.setEncryptMode(ServerUtil.ENCRYPT_MODE);
        certificate.setHashType(ServerUtil.HASH_ALGORITHM);
        certificate.setDateTime(Timestamp.valueOf(request.getDateTime()));
        return certificate;
    }
}

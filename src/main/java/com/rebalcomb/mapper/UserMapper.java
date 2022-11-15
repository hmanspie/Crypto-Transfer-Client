package com.rebalcomb.mapper;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rebalcomb.config.ServerUtil;
import com.rebalcomb.crypto.RSAUtil;
import com.rebalcomb.model.dto.SignUpRequest;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.model.entity.enums.Status;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class UserMapper {

    public static User mapUserRequest(SignUpRequest request) {
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.withDefaults().hashToString(12, request.getPassword().toCharArray()));
        user.setRole(Role.USER);
        user.setStatus(Status.ACTIVE);
        user.setSecret(BigInteger.probablePrime(256, new SecureRandom()).toString());
        user.setRegTime(Timestamp.valueOf(LocalDateTime.now().format(formatter)));
        return user;
    }
}

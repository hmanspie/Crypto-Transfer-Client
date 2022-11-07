package com.rebalcomb.mapper;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rebalcomb.model.dto.SignUpRequest;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.model.entity.enums.Status;
import org.springframework.stereotype.Component;
import java.math.BigInteger;
import java.security.SecureRandom;

@Component
public class UserMapper {

    public static User mapUserRequest(SignUpRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.withDefaults().hashToString(12, request.getPassword().toCharArray()));
        user.setRole(Role.USER);
        user.setStatus(Status.ACTIVE);
        user.setSecret(BigInteger.probablePrime(32, new SecureRandom()).toString());
        return user;
    }
}

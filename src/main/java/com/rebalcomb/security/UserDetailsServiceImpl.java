package com.rebalcomb.security;

import com.rebalcomb.config.ServerUtil;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private final Logger logger = LogManager.getLogger(UserDetailsServiceImpl.class);
    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }


    // todo якщо користувача не знайдено виникає виняток (No value present)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username).get();
        return SecurityUser.fromUser(user);
    }
}

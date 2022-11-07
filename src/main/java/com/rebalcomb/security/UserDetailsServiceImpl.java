package com.rebalcomb.security;

import com.rebalcomb.crypto.RSAUtil;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private final Logger logger = LogManager.getLogger(UserDetailsServiceImpl.class);
    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username).get();
        if(RSAUtil.KEY_PAIR == null)
            getPublicKeyFromMainServer();
        return SecurityUser.fromUser(user);
    }

    public void getPublicKeyFromMainServer() {
        RSAUtil.KEY_PAIR = userService.getPublicKey("1").block();
        assert RSAUtil.KEY_PAIR != null;
        logger.info("Get public key: " +  RSAUtil.KEY_PAIR.getPublicKey() + " successfully!");
    }
}

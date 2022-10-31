package com.rebalcomb.security;

import com.rebalcomb.crypto.RSAUtil;
import com.rebalcomb.model.entity.User;
import com.rebalcomb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User doesn't exists"));
        if(user != null && RSAUtil.KEY_PAIR == null) {
            userService.getPublicKeyFromMainServer();
        }
        return SecurityUser.fromUser(user);
    }
}

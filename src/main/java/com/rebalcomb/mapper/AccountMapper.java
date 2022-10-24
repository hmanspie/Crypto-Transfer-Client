package com.rebalcomb.mapper;

import com.rebalcomb.controllers.AccountController;
import com.rebalcomb.crypto.AESUtil;
import com.rebalcomb.crypto.Hash;
import com.rebalcomb.crypto.rsa.RSAUtil;
import com.rebalcomb.model.dto.AccountSignUpRequest;
import com.rebalcomb.model.entity.Account;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;

@Component
public class AccountMapper {

    public static Account mapAccountRequest(AccountSignUpRequest request) {
        Account account = new Account();
        account.setLogin(request.getLogin());
        account.setEmail(request.getEmail());
        account.setFullName(request.getFullName());
        account.setPasswd(Hash.getSHA512(request.getPasswd()));
        account.setIsAdmin(false);
        account.setSecret("0");
        return account;
    }
}

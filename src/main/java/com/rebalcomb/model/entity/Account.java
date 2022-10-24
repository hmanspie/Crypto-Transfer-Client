package com.rebalcomb.model.entity;

import com.rebalcomb.model.dto.KeyPairRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private Integer id;

    private String email;

    private String fullName;

    private String login;

    private String passwd;

    private Boolean isAdmin;

    private String secret;

    private List<Message> incomingMessageList;

    private List<Message> outcomingMessageList;

    private KeyPairRequest keyPairRequest;

}

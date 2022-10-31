package com.rebalcomb.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.model.entity.enums.Status;
import lombok.Data;

import javax.persistence.*;
import java.util.List;


@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Role role;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Status status;

    @Column(nullable = false)
    private Boolean isAdmin;

    @Column(nullable = false, length = 256)
    private String secret;

    @JsonIgnore
    @OneToMany(mappedBy = "to", fetch = FetchType.LAZY)
    private List<Message> incomingMessageList;

    @JsonIgnore
    @OneToMany(mappedBy = "from", fetch = FetchType.LAZY)
    private List<Message> outcomingMessageList;


}

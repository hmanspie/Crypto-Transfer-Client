package com.rebalcomb.model.entity;


import com.rebalcomb.model.entity.enums.Role;
import com.rebalcomb.model.entity.enums.Status;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String owner;

    @Column(nullable = false)
    private String publicKey;

    @Column(nullable = false)
    private Integer keyLength;

    @Column(nullable = false)
    private String algorithm;

    @Column(nullable = false)
    private String encryptMode;

    @Column(nullable = false)
    private String hashType;

    @Column(nullable = false)
    private Timestamp dateTime;

}

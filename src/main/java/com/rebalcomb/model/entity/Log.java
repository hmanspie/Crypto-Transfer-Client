package com.rebalcomb.model.entity;

import com.rebalcomb.model.entity.enums.TypeLog;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private TypeLog type;

    @Column(nullable = false)
    private Timestamp date_time;

    @Column(nullable = false, length = 255)
    private String info;
}

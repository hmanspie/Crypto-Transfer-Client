package com.rebalcomb.model.entity;


import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Message {
    private Integer id;

    private Account from;

    private Account to;

    private String title;

    private String body;

    private String date_time;

    private Boolean is_read;

}

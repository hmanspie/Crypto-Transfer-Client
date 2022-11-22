package com.rebalcomb.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String user_from;

    @Column(nullable = false, length = 40)
    private String user_to;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private Timestamp date_time;

    @JsonIgnore
    @Column(nullable = false)
    private Boolean is_send;

    @Column(nullable = false)
    private String hash;

    @OneToOne(fetch = FetchType.EAGER)
    private Certificate certificate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Message message = (Message) o;
        return id != null && Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

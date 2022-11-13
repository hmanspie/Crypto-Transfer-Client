package com.rebalcomb.repositories;

import com.rebalcomb.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRopository extends JpaRepository<Message, Long> {

    Message findTopByOrderByIdDesc();
    @Query("SELECT message FROM Message message WHERE message.user_from = ?1")
    List<Message> findAllByUsernameFrom(String username);

    @Query("SELECT message FROM Message message WHERE message.user_to = ?1")
    List<Message> findAllByUsernameTo(String username);


}

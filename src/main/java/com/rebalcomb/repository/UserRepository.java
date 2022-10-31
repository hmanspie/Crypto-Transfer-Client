package com.rebalcomb.repository;

import com.rebalcomb.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT user FROM User user WHERE user.username = ?1 ")
    Optional<User> findByUsername(String username);

    @Query("SELECT user.secret FROM User user WHERE user.username = ?1")
    String findSecretByUsername(String username);

    @Query("SELECT user.username FROM User user")
    List<String> findAllUsername();
}

package com.rebalcomb.repositories;

import com.rebalcomb.model.entity.User;
import com.rebalcomb.model.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT user FROM User user WHERE user.username = ?1 ")
    Optional<User> findByUsername(String username);

    @Query("SELECT user FROM User user WHERE user.email = ?1 ")
    Optional<User> findByEmail(String email);

    @Query("SELECT user.secret FROM User user WHERE user.username = ?1")
    String findSecretByUsername(String username);

    @Query("DELETE FROM User user WHERE user.username = ?1")
    void deleteByUsername(String username);

    @Query("SELECT user.role FROM User user WHERE user.username = ?1")
    Role isAdmin(String username);

    @Query("SELECT user.username FROM User user")
    List<String> findAllUsername();

}

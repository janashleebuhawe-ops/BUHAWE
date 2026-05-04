package com.ordering.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ordering.system.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // For duplicate checks
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // ✅ For email verification — finds user by their token
    User findByVerificationToken(String token);
}
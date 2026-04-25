package com.securetask.taskmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.securetask.taskmanager.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring reads this method name and writes the SQL automatically
    // Optional means it might return a User, or it might return nothing
    Optional<User> findByEmail(String email);

    // Returns true/false — used to check if email is already taken
    boolean existsByEmail(String email);
}

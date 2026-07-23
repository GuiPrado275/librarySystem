package com.guilherme.librarySystem.repositories;

import com.guilherme.librarySystem.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); //used in login (find user by email)

    boolean existsByEmail(String email); //used on creation, to validate duplicate email
}
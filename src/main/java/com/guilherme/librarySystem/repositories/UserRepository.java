package com.guilherme.librarySystem.repositories;

import com.guilherme.librarySystem.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Transactional(readOnly = true)
    User findByEmail(String email); //to search for the user by email

    boolean existsByEmail(String email); //used on creation, to validate duplicate email
}
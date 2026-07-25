package com.guilherme.librarySystem.repositories;

import com.guilherme.librarySystem.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByBookIdAndIsActiveTrueOrderByReservationDateAsc(Long bookId); //full waiting queue for a book, in order

    Optional<Reservation> findFirstByBookIdAndIsActiveTrueOrderByReservationDateAsc(Long bookId); //next in line, used when a copy is returned

    Optional<Reservation> findByUserIdAndBookIdAndIsActiveTrue(Long userId, Long bookId); //a specific user's active reservation for a book

    boolean existsByUserIdAndBookIdAndIsActiveTrue(Long userId, Long bookId); //avoid duplicate reservations

    List<Reservation> findByUserIdAndIsActiveTrue(Long userId); //a user's active reservations
}
package com.guilherme.librarySystem.repositories;

import com.guilherme.librarySystem.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId); //loan history for a specific user

    List<Loan> findByUserIdAndIsReturnedFalse(Long userId); //active loans for a user (used to check the limit)

    List<Loan> findByIsReturnedFalse(); //all loans not yet returned (used by the Scheduler for due-date notifications)

    boolean existsByBookIdAndIsReturnedFalse(Long bookId); //check if a specific book is currently on loan
}

package com.guilherme.librarySystem.repositories;

import com.guilherme.librarySystem.models.Loan;
import com.guilherme.librarySystem.models.dto.BookLoanCountDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId); //loan history for a specific user

    List<Loan> findByUserIdAndIsReturnedFalse(Long userId); //active loans for a user (used to check the limit)

    List<Loan> findByIsReturnedFalse(); //all loans not yet returned (used by the Scheduler for due-date notifications)

    boolean existsByBookIdAndIsReturnedFalse(Long bookId); //check if a specific book is currently on loan

    long countByUserIdAndIsReturnedFalse(Long userId); //used to enforce the max simultaneous loans per user

    boolean existsByUserIdAndBookIdAndIsReturnedFalse(Long userId, Long bookId); //avoid duplicate loan of the same book by the same user

    List<Loan> findByIsReturnedFalseAndDueDateBefore(LocalDate date); //overdue loans (used by the Scheduler and to block new loans)

    List<Loan> findByIsReturnedFalseAndDueDateBetween(LocalDate start, LocalDate end); //loans about to expire (used by the Scheduler)

    @Query("SELECT new com.guilherme.librarySystem.models.dto.BookLoanCountDTO(l.book, COUNT(l)) " +
            "FROM Loan l GROUP BY l.book ORDER BY COUNT(l) DESC")
    List<BookLoanCountDTO> findMostBorrowedBooks(Pageable pageable); //report: most borrowed books
}
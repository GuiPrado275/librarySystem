package com.guilherme.librarySystem.repositories;

import com.guilherme.librarySystem.models.Loan;
import com.guilherme.librarySystem.models.dto.BookLoanCountDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId); //loan history for a specific user

    List<Loan> findByUserIdAndIsReturnedFalse(Long userId); //active loans for a user (used to check the limit)

    List<Loan> findByIsReturnedFalse(); //all loans not yet returned (used by findAllActive, admin/librarian listing)

    long countByUserIdAndIsReturnedFalse(Long userId); //used to enforce the max simultaneous loans per user

    boolean existsByUserIdAndBookIdAndIsReturnedFalse(Long userId, Long bookId); //avoid duplicate loan of the same book by the same user

    @Query("SELECT new com.guilherme.librarySystem.models.dto.BookLoanCountDTO(l.book, COUNT(l)) " +
            "FROM Loan l GROUP BY l.book ORDER BY COUNT(l) DESC")
    List<BookLoanCountDTO> findMostBorrowedBooks(Pageable pageable); //report: most borrowed books
}

package com.guilherme.librarySystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = Loan.TABLE_NAME) //database table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Loan {

    public static final String TABLE_NAME = "loans";

    @Id
    @Column(name = "id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY) //id is random (id)
    private Long id;

    @ManyToOne //Many loans for one user
    @JoinColumn(name = "userId", nullable = false, updatable = false) //this is for make reference of "user_id"
    private User user;

    @ManyToOne //Many loans for one book
    @JoinColumn(name = "bookId", nullable = false, updatable = false) //this is for make reference of "book_id"
    private Book book;

    @Column(name = "loanDate", nullable = false)
    @NotNull(message = "A data de empréstimo não pode ficar em branco.")
    private LocalDate loanDate;

    @Column(name = "dueDate", nullable = false)
    @NotNull(message = "A data prevista de devolução não pode ficar em branco.")
    private LocalDate dueDate;

    @Column(name = "returnDate")
    private LocalDate returnDate; //null means the book has not been returned yet

    @Column(name = "fine")
    private BigDecimal fine = BigDecimal.ZERO; //late return fine, calculated when the book is returned

    @Column(name = "isReturned", nullable = false)
    private boolean isReturned;
}
package com.guilherme.librarySystem.models.dto;

import com.guilherme.librarySystem.models.Book;

//Used by LoanRepository#findMostBorrowedBooks to build the "most borrowed books" report
public record BookLoanCountDTO(Book book, Long totalLoans) {}
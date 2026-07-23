package com.guilherme.librarySystem.repositories;

import com.guilherme.librarySystem.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByIsActiveTrue(); //list only active books in the collection

    List<Book> findByTitleContainingIgnoreCase(String title); //search by title (partial, case insensitive)

    List<Book> findByAuthorContainingIgnoreCase(String author); //search by author
}
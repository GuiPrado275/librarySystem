package com.guilherme.librarySystem.controllers;

import com.guilherme.librarySystem.models.Book;
import com.guilherme.librarySystem.models.dto.BookCreateDTO;
import com.guilherme.librarySystem.models.dto.BookLoanCountDTO;
import com.guilherme.librarySystem.models.dto.BookUpdateDTO;
import com.guilherme.librarySystem.services.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/book")
@Validated
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> findAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author) {

        List<Book> books;
        if (title != null) {
            books = this.bookService.findByTitle(title);
        } else if (author != null) {
            books = this.bookService.findByAuthor(author);
        } else {
            books = this.bookService.findAllActive();
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable Long id) {
        Book book = this.bookService.findById(id);
        return ResponseEntity.ok().body(book);
    }

    @GetMapping("/most-borrowed")
    public ResponseEntity<List<BookLoanCountDTO>> findMostBorrowedBooks(
            @RequestParam(defaultValue = "5") int topN) {
        List<BookLoanCountDTO> report = this.bookService.findMostBorrowedBooks(topN);
        return ResponseEntity.ok(report);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody BookCreateDTO bookCreateDTO) {
        Book book = this.bookService.fromDTO(bookCreateDTO);
        Book newBook = this.bookService.create(book);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newBook.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody BookUpdateDTO bookUpdateDTO) {
        Book book = this.bookService.fromDTO(bookUpdateDTO);
        this.bookService.update(id, book);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

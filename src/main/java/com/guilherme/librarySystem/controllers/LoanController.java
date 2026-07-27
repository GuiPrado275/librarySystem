package com.guilherme.librarySystem.controllers;

import com.guilherme.librarySystem.models.Loan;
import com.guilherme.librarySystem.models.Reservation;
import com.guilherme.librarySystem.models.dto.LoanCreateDTO;
import com.guilherme.librarySystem.services.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/loan")
@Validated
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping("/{id}")
    public ResponseEntity<Loan> findById(@PathVariable Long id) {
        Loan loan = this.loanService.findById(id);
        return ResponseEntity.ok().body(loan);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Loan>> findByUser(@PathVariable Long userId) {
        List<Loan> loans = this.loanService.findByUser(userId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Loan>> findActiveByUser(@PathVariable Long userId) {
        List<Loan> loans = this.loanService.findActiveByUser(userId);
        return ResponseEntity.ok(loans);
    }

    // LoanService.findAllActive already restricts this to ADMIN/LIBRARIAN (checkStaff()).
    @GetMapping("/active")
    public ResponseEntity<List<Loan>> findAllActive() {
        List<Loan> loans = this.loanService.findAllActive();
        return ResponseEntity.ok(loans);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody LoanCreateDTO loanCreateDTO) {
        Loan newLoan = this.loanService.create(loanCreateDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newLoan.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Loan> returnLoan(@PathVariable Long id) {
        Loan returnedLoan = this.loanService.returnLoan(id);
        return ResponseEntity.ok(returnedLoan);
    }

    // Reservation

    @PostMapping("/reservation")
    public ResponseEntity<Void> reserve(@Valid @RequestBody LoanCreateDTO loanCreateDTO) {
        Reservation newReservation = this.loanService.reserve(loanCreateDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newReservation.getId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    // LoanService.cancelReservation already restricts this to the reservation's own user, or ADMIN/LIBRARIAN.
    @DeleteMapping("/reservation/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        this.loanService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    // LoanService.findReservationsByUser already restricts this to that user themselves, or ADMIN/LIBRARIAN.
    @GetMapping("/reservation/user/{userId}")
    public ResponseEntity<List<Reservation>> findReservationsByUser(@PathVariable Long userId) {
        List<Reservation> reservations = this.loanService.findReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }

    // ADMIN/LIBRARIAN
    @GetMapping("/reservation/book/{bookId}")
    public ResponseEntity<List<Reservation>> findQueueForBook(@PathVariable Long bookId) {
        List<Reservation> queue = this.loanService.findQueueForBook(bookId);
        return ResponseEntity.ok(queue);
    }

}

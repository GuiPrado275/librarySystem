package com.guilherme.librarySystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = Reservation.TABLE_NAME) //database table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Reservation {

    public static final String TABLE_NAME = "reservations";

    @Id
    @Column(name = "id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY) //id is random (id)
    private Long id;

    @ManyToOne //Many reservations for one user
    @JoinColumn(name = "userId", nullable = false, updatable = false)
    private User user;

    @ManyToOne //Many reservations for one book
    @JoinColumn(name = "bookId", nullable = false, updatable = false)
    private Book book;

    @Column(name = "reservationDate", nullable = false)
    @NotNull(message = "A data da reserva não pode ficar em branco.")
    private LocalDateTime reservationDate;

    @Column(name = "isActive", nullable = false)
    private boolean isActive; //true while the user is in the waiting queue; false once cancelled or fulfilled

    @Column(name = "isNotified", nullable = false)
    private boolean isNotified; //true once the user has been notified that the book is available for them
}
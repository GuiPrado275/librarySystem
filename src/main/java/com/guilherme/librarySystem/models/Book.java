package com.guilherme.librarySystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = Book.TABLE_NAME) //database table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Book {

    public static final String TABLE_NAME = "books";

    @Id
    @Column(name = "id", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY) //id is random (id)
    private Long id;

    @Column(name = "title", length = 50, nullable = false)
    @Size(min = 1, max = 50, message = "O título deve ter entre 1 e 50 caracteres.")
    @NotBlank(message = "O título não pode ficar em branco.")
    private String title;

    @Column(name = "author", length = 50, nullable = false)
    @Size(min = 1, max = 50, message = "O autor deve ter entre 1 e 50 caracteres.")
    @NotBlank(message = "O autor não pode ficar em branco.")
    private String author;

    @Column(name = "category", length = 50)
    @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres.")
    private String category;

    @Column(name = "publicationYear")
    @Min(value = 1000, message = "O ano de publicação deve ser válido.")
    @Max(value = 2026, message = "O ano de publicação deve ser válido.")
    private Integer publicationYear;

    @Column(name = "totalCopies", nullable = false)
    @NotNull(message = "A quantidade total de exemplares não pode ficar em branco.")
    @Min(value = 1, message = "A quantidade total de exemplares deve ser no mínimo 1.")
    private Integer totalCopies;

    @Column(name = "availableCopies", nullable = false)
    @NotNull(message = "A quantidade de exemplares disponíveis não pode ficar em branco.")
    @Min(value = 0, message = "A quantidade de exemplares disponíveis não pode ser negativa.")
    private Integer availableCopies;

    @Column(name = "registrationDate", nullable = false)
    @NotNull(message = "Data de registro não pode ficar em branco.")
    private LocalDateTime registrationDate;

    @Column(name = "isActive", nullable = false)
    private boolean isActive;
}

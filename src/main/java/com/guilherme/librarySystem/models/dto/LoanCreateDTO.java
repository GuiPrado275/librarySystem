package com.guilherme.librarySystem.models.dto;

import jakarta.validation.constraints.NotNull;

public record LoanCreateDTO(
        @NotNull(message = "O usuário é obrigatório.")
        Long userId,

        @NotNull(message = "O livro é obrigatório.")
        Long bookId
) {}
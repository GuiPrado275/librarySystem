package com.guilherme.librarySystem.models.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookCreateDTO(
        @NotBlank(message = "O título não pode ficar em branco.")
        @Size(min = 1, max = 50, message = "O título deve ter entre 1 e 50 caracteres.")
        String title,

        @NotBlank(message = "O autor não pode ficar em branco.")
        @Size(min = 1, max = 50, message = "O autor deve ter entre 1 e 50 caracteres.")
        String author,

        @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres.")
        String category,

        @Min(value = 1000, message = "O ano de publicação deve ser válido.")
        @Max(value = 2026, message = "O ano de publicação deve ser válido.")
        Integer publicationYear,

        @NotNull(message = "A quantidade total de exemplares não pode ficar em branco.")
        @Min(value = 1, message = "A quantidade total de exemplares deve ser no mínimo 1.")
        Integer totalCopies
) {}
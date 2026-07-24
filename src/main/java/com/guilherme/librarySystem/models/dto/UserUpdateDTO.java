package com.guilherme.librarySystem.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(

        Long id,

        @NotBlank(message = "A senha não pode estar em branco.")
        @Size(min = 8, max = 60, message = "A senha deve ter entre 8 e 60 caracteres.")
        String password
) {}
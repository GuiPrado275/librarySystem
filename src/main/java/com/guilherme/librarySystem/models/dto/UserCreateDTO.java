package com.guilherme.librarySystem.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
        @NotBlank(message = "O nome não pode estar em branco.")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
        String name,

        @NotBlank(message = "O e-mail não pode estar em branco.")
        @Email(message = "O e-mail deve ser válido!")
        @Size(min = 5, max = 50, message = "O e-mail deve ter entre 5 e 50 caracteres.")
        String email,

        @NotBlank(message = "A senha não pode estar em branco.")
        @Size(min = 8, max = 60, message = "A senha deve ter entre 8 e 60 caracteres.")
        String password
) {}
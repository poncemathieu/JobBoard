package com.example.JobBoard.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotNull(message = "jobId est obligatoire")
        @NotBlank(message = "jobId est obligatoire")
        String jobId,

        @NotBlank(message = "Le nom du candidat est obligatoire")
        String candidateName,

        @Email(message = "Email invalide")
        @NotBlank(message = "L'email du candidat est obligatoire")
        String candidateEmail,

        String message) {
}

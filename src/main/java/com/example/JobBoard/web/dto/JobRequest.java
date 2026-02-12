package com.example.JobBoard.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JobRequest (
        @NotBlank(message = "Le titre est obligatoire")
        String title,

        @NotBlank(message = "La société est obligatoire")
        String company,

        @NotBlank(message = "La localisation est obligatoire")
        String location,

        @NotNull(message = "Le salaire minimum est obligatoire")
        @Min(value = 1, message = "Le salaire minimum doit être positif")
        Integer salaryMin,

        @NotNull(message = "Le salaire maximum est obligatoire")
        @Min(value = 1, message = "Le salaire maximum doit être positif")
        Integer salaryMax
) {}

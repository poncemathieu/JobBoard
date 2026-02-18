package com.example.JobBoard.web.dto;

import com.example.JobBoard.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(@NotNull(message = "Status est obligatoire")
                                             ApplicationStatus status) {}

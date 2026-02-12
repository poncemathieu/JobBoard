package com.example.JobBoard.domain;

import java.time.Instant;

public record Application(
        String id,
        String jobId,
        String candidateName,
        String candidateEmail,
        String message,
        int score,
        String status,
        Instant createdAt
) {}

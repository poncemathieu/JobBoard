package com.example.JobBoard.domain;

public record Job(
        String id,
        String title,
        String company,
        String location,
        Integer salaryMin,
        Integer salaryMax
) {}

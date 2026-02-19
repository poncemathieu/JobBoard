package com.example.JobBoard.web.dto;

import com.example.JobBoard.domain.Job;

import java.util.List;

public record JobsPageResponse(
        List<Job> items,
        int limit,
        int offset,
        long total
) {}

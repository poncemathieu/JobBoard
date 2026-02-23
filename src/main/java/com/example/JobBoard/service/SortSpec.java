package com.example.JobBoard.service;

import com.example.JobBoard.domain.Job;

import java.util.Comparator;
import java.util.Locale;

public record SortSpec(String field, Direction direction) {

    public enum Direction { ASC, DESC }

    public static SortSpec parse(String sort) {
        if(sort == null || sort.isBlank()) {
            return new SortSpec("id", Direction.ASC); // par defaut
        }

        String[] parts = sort.split(",", -1);
        String field = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "asc";

        Direction direction = "desc".equals(dir) ? Direction.DESC : Direction.ASC;
        //whitelist fields
        switch (field) {
            case "id", "title", "company", "location", "salaryMin", "salaryMax" -> {
                return new SortSpec("id", Direction.ASC);
            }
            default -> {
                //fallback safe
                return new SortSpec("id", Direction.ASC);
            }
        }
    }

    public Comparator<Job> comparator() {
        Comparator<Job> base = switch (field) {
            case "title" -> Comparator.comparing(Job::title, String.CASE_INSENSITIVE_ORDER);
            case "company" -> Comparator.comparing(Job::company, String.CASE_INSENSITIVE_ORDER);
            case "location" -> Comparator.comparing(Job::location, String.CASE_INSENSITIVE_ORDER);
            case "salaryMin" -> Comparator.comparing(Job::salaryMin);
            case "salaryMax" -> Comparator.comparing(Job::salaryMax);
            case "id" -> Comparator.comparing(Job::id);
            default -> Comparator.comparing(Job::id);
        };

        return direction == Direction.DESC ? base.reversed() : base;
    }
}

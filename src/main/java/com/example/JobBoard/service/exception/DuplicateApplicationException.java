package com.example.JobBoard.service.exception;

public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException() {
        super("Application already exists for this job and email");
    }
}

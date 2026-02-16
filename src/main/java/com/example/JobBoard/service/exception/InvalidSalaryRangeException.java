package com.example.JobBoard.service.exception;

public class InvalidSalaryRangeException extends RuntimeException{
    public InvalidSalaryRangeException() {
        super("salaryMax must be greater than or equal to salaryMin");
    }
}

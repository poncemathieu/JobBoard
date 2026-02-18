package com.example.JobBoard.service.exception;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(String applicationId) {
        super("Application not found for this : " + applicationId);
    }
}

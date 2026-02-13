package com.example.JobBoard.service.exception;

public class JobNotFoundException extends RuntimeException {
    private final String JobId;

    public JobNotFoundException(String JobId) {
        super("Job Not Found" + JobId);
        this.JobId = JobId;
    }

    public String getJobId() {
        return JobId;
    }
}

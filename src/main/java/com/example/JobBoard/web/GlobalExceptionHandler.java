package com.example.JobBoard.web;

import com.example.JobBoard.service.exception.JobNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(WebExchangeBindException ex) {
        Map<String, String> fieldErrors = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (msg1, msg2) -> msg1
                ));

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);

        return body;
    }

    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleInput(ServerWebInputException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Requête invalide (JSON ou champs incorrects)");
        return body;
    }

    @ExceptionHandler(JobNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public java.util.Map<String, Object> handleJobNotFound(JobNotFoundException ex) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("message", ex.getMessage());
        body.put("jobId", ex.getJobId());
        return body;
    }
}

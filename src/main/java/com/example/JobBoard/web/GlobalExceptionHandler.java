package com.example.JobBoard.web;

import com.example.JobBoard.filters.TraceIdFilter;
import com.example.JobBoard.service.exception.ApplicationNotFoundException;
import com.example.JobBoard.service.exception.DuplicateApplicationException;
import com.example.JobBoard.service.exception.InvalidSalaryRangeException;
import com.example.JobBoard.service.exception.JobNotFoundException;
import com.example.JobBoard.web.dto.UpdateApplicationStatusRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, Object>> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            Map<String, String> fieldErrors = ex.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            DefaultMessageSourceResolvable::getDefaultMessage,
                            (msg1, msg2) -> msg1
                    ));
            Map<String, Object> body = new HashMap<>();
            body.put("traceId", traceId);
            body.put("statut", HttpStatus.BAD_REQUEST.value());
            body.put("errors", fieldErrors);

            return Mono.just(body);
        });
    }

    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, Object>> handleInput(ServerWebInputException ex, ServerWebExchange exchange) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            Map<String, Object> body = new HashMap<>();
            body.put("traceId", traceId);
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("message", "Requête invalide (JSON ou champs incorrects)");
            return Mono.just(body);
        });

    }

    @ExceptionHandler(JobNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, Object>> handleJobNotFound(JobNotFoundException ex, ServerWebExchange exchange) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            Map<String, Object> body = new HashMap<>();
            body.put("traceId", traceId);
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("message", ex.getMessage());
            body.put("jobId", ex.getJobId());
            return Mono.just(body);
        });
    }

    @ExceptionHandler(InvalidSalaryRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, Object>> handleInvalidSalaryRange(InvalidSalaryRangeException ex, ServerWebExchange exchange) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            Map<String, Object> body = new HashMap<>();
            body.put("traceId", traceId);
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("message", ex.getMessage());
            return Mono.just(body);
        });
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<Map<String, Object>> handleDuplicateApplication(DuplicateApplicationException ex, ServerWebExchange exchange) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            Map<String, Object> body = new HashMap<>();
            body.put("traceId", traceId);
            body.put("status", HttpStatus.CONFLICT.value());
            body.put("message", ex.getMessage());
            return Mono.just(body);
        });
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, Object>> handleApplicationNotFound(ApplicationNotFoundException ex, ServerWebExchange exchange) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(TraceIdFilter.TRACE_ID_KEY, "no-trace");

            Map<String, Object> body = new HashMap<>();
            body.put("traceId", traceId);
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("message", ex.getMessage());
            body.put("applicationId", ex.getMessage());
            return Mono.just(body);
        });
    }
}

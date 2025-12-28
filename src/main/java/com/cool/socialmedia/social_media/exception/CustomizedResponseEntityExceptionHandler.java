package com.cool.socialmedia.social_media.exception;

import java.time.LocalDateTime;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final Environment environment;

    public CustomizedResponseEntityExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDev = false;
        for (String profile : activeProfiles) {
            if ("dev".equals(profile)) {
                isDev = true;
            }
        }

        ErrorDetails errorDetails;
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            status = HttpStatus
                    .resolve(((org.springframework.web.server.ResponseStatusException) ex).getStatusCode().value());
        } else if (ex instanceof jakarta.validation.ConstraintViolationException) {
            status = HttpStatus.BAD_REQUEST;
        }

        if (isDev) {
            // Show trace, error, path only when it's mode dev
            errorDetails = new ErrorDetails(LocalDateTime.now(),
                    ex.getMessage(), request.getDescription(true));
        } else {
            // Good error message for the prod configuration
            if (ex instanceof org.springframework.web.server.ResponseStatusException) {
                errorDetails = new ErrorDetails(LocalDateTime.now(),
                        ((org.springframework.web.server.ResponseStatusException) ex).getReason(),
                        request.getDescription(false));
            } else if (ex instanceof jakarta.validation.ConstraintViolationException) {
                errorDetails = new ErrorDetails(LocalDateTime.now(),
                        "Validation Failed: " + ex.getMessage(), request.getDescription(false));
            } else {
                errorDetails = new ErrorDetails(LocalDateTime.now(),
                        "An unexpected error occurred. Please contact support.", request.getDescription(false));
            }
        }

        return new ResponseEntity<>(errorDetails, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String errorMessage = error.getDefaultMessage();
            errors.append(errorMessage).append(". ");
        });

        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                "Total Errors: " + ex.getErrorCount() + " First Error: " + errors.toString(),
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}

package org.example.webhookdelivery.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for application-specific errors.
 */
public class CustomException extends RuntimeException {

    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

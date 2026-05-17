package org.example.backend.common.exception;

public class PinVerificationException extends RuntimeException {
    public PinVerificationException(String message) {
        super(message);
    }
}

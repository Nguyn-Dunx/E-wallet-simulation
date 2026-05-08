package org.example.backend.common.exception;

public class ElementAlreadyExistsException extends RuntimeException {

    public ElementAlreadyExistsException(String message) {
        super(message);
    }
}
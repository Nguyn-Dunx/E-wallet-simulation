package org.example.backend.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Internal server error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {

        log.error("Internal Server Error: ", ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("The system is currently undergoing maintenance; please try again later.")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    // Wrong URL / endpoint not found
    @ExceptionHandler({
            NoResourceFoundException.class,
            NoHandlerFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(Exception ex) {

        log.warn("URL not found: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message("The URL does not exist, please try again.")
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    // Duplicate resource
    @ExceptionHandler(ElementAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleElementAlreadyExists(
            ElementAlreadyExistsException ex
    ) {

        log.warn("Element already exists: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    // Business/runtime exception
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex
    ) {

        log.error("Business Exception: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage()) // Sẽ nhận được chuỗi "Không tìm thấy người dùng..."
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().iterator().next().getMessage();

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .build();

        return ResponseEntity.badRequest().body(response);
    }
}
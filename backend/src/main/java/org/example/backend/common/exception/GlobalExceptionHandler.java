package org.example.backend.common.exception;

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

    // error chung
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Internal Server Error: ", ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // Mã 500
                .message("The system is currently undergoing maintenance; please try again later.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // sai url
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(Exception ex) {
        log.warn("URL not found: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.NOT_FOUND.value()) // Mã 404
                .message("The URL does not exist, please try again.")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ElementAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleElementAlreadyExists(ElementAlreadyExistsException ex) {
        log.warn("Element already exists: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.CONFLICT.value()) // Mã 409
                .message(ex.getMessage())          // Ví dụ: "Phone already exists"
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Business Exception: {} ", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value()) // Mã 400
                .message(ex.getMessage())             // Ví dụ: "Insufficient funds"
                .build();

        return ResponseEntity.badRequest().body(response);
    }


}

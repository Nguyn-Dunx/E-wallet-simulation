package org.example.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) //data = null -> ko return
public class ApiResponse<T> {
    private Instant timestamp;
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message,T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .code(200)
                .message(message)
                .data(data)
                .build();
    }
}

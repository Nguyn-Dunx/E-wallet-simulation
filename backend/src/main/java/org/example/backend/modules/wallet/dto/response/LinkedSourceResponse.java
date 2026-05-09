package org.example.backend.modules.wallet.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class LinkedSourceResponse {
    private UUID id;
    private String bankName;
    private String accountNumber; // STK đã che
    private String status;
}

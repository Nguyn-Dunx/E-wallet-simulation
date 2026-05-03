package org.example.backend.modules.wallet.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data   //sinh ra full constructor, getter/setter , toString, ...
public class WalletResponse {

    private UUID id;
    private BigDecimal balance;
    private String currency;
    private String status;

}

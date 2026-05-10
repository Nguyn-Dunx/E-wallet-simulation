package org.example.backend.modules.transaction.mapper;

import org.example.backend.modules.transaction.dto.response.TransactionResponse;
import org.example.backend.modules.transaction.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponse toResponse(Transaction transaction);
}

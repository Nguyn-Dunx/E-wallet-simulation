package org.example.backend.modules.wallet.mapper;

import org.example.backend.modules.wallet.dto.response.LinkedSourceResponse;
import org.example.backend.modules.wallet.entity.LinkedSource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LinkedSourceMapper {
    @Mapping(target = "accountNumber", source = "accountNumber", qualifiedByName = "maskAccountNumber")
    LinkedSourceResponse toResponse(LinkedSource entity);

    List<LinkedSourceResponse> toResponseList(List<LinkedSource> entities);

    // Data Masking: Chỉ giữ lại 4 số cuối
    @Named("maskAccountNumber")
    default String maskAccountNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.length() <= 4) {
            return rawNumber;
        }
        String lastFourDigits = rawNumber.substring(rawNumber.length() - 4);
        return "*".repeat(rawNumber.length() - 4) + lastFourDigits;
    }
}

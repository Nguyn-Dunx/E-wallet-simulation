package org.example.backend.modules.wallet.mapper;

import org.example.backend.modules.wallet.dto.response.WalletResponse;
import org.example.backend.modules.wallet.entity.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    WalletResponse toResponse(Wallet entity); // return 1 WalletResponse (ko can impl - same Spring data Jpa)

}

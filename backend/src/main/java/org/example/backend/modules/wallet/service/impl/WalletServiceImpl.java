package org.example.backend.modules.wallet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.modules.wallet.dto.request.LinkBankRequest;
import org.example.backend.modules.wallet.dto.response.LinkedSourceResponse;
import org.example.backend.modules.wallet.dto.response.WalletResponse;
import org.example.backend.modules.wallet.entity.LinkedSource;
import org.example.backend.modules.wallet.entity.Wallet;
import org.example.backend.modules.wallet.mapper.LinkedSourceMapper;
import org.example.backend.modules.wallet.mapper.WalletMapper;
import org.example.backend.modules.wallet.repository.LinkedSourceRepository;
import org.example.backend.modules.wallet.repository.WalletRepository;
import org.example.backend.modules.wallet.service.WalletService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final LinkedSourceRepository linkedSourceRepository;
    private final LinkedSourceMapper linkedSourceMapper;


    @Override
    public WalletResponse getMyWallet(UUID userId) {

        log.info("Fetching wallet info for user: {}", userId);
        // Retrieve DB tìm ví
        Wallet wallet = walletRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for this user"));

        return walletMapper.toResponse(wallet); // Map Entity sang DTO
    }

    @Override
    public List<LinkedSourceResponse> getMyLinkedSources(UUID userId) {
        Wallet wallet = walletRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for this user"));

        List<LinkedSource> sources = linkedSourceRepository.findByWalletIdAndDeletedAtIsNull(wallet.getId());
        return linkedSourceMapper.toResponseList(sources);
    }

    @Override
    public LinkedSourceResponse linkBankAccount(UUID userId, LinkBankRequest request) {
        Wallet wallet = walletRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // check linkBankAccount before ?
        boolean exists = linkedSourceRepository.existsByWalletIdAndAccountNumberAndBankNameAndDeletedAtIsNull(
                wallet.getId(), request.getAccountNumber(), request.getBankName());

        if (exists) {
            throw new RuntimeException("This bank account is already linked to your wallet");
        }

        LinkedSource newSource = LinkedSource.builder()
                .walletId(wallet.getId())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .build();

        LinkedSource savedSource = linkedSourceRepository.save(newSource);
        return linkedSourceMapper.toResponse(savedSource);
    }

    @Override
    public void unlinkBankAccount(UUID userId, UUID sourceId) {
        Wallet wallet = walletRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        LinkedSource source = linkedSourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("Linked source not found"));

        // Bảo mật: Đảm bảo nguồn tiền này thực sự thuộc về ví của User đang request
        if (!source.getWalletId().equals(wallet.getId())) {
            throw new RuntimeException("You do not have permission to unlink this source");
        }

        // Thực hiện xóa mềm
        source.softDelete();
        source.setStatus("DISCONNECTED");
        linkedSourceRepository.save(source);
    }
}

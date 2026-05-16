package org.example.backend.modules.identity.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class OtpUtils {
    public String createOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}

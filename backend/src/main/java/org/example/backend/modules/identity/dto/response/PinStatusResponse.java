package org.example.backend.modules.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PinStatusResponse {
    private boolean hasPin;
}

package com.example.logisquare_server.safety.dto;

import java.math.BigDecimal;

public record AiCctvSafetyDetectionRequest(
        String label,
        BigDecimal confidence
) {
}

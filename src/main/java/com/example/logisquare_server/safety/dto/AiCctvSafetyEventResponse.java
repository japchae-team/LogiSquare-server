package com.example.logisquare_server.safety.dto;

import java.time.LocalDateTime;

public record AiCctvSafetyEventResponse(
        Long id,
        String status,
        LocalDateTime occurredAt
) {
}

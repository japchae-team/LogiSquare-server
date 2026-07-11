package com.example.logisquare_server.safety.dto;

import java.time.LocalDateTime;

public record SafetyEventActionResponse(
        Long eventId,
        String status,
        Long workerId,
        String workerEmployeeNo,
        String workerName,
        LocalDateTime assignedAt,
        LocalDateTime resolvedAt
) {
}

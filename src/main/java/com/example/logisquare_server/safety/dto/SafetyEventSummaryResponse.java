package com.example.logisquare_server.safety.dto;

import java.time.LocalDateTime;

public record SafetyEventSummaryResponse(
        Long eventId,
        String eventType,
        String eventTypeLabel,
        String status,
        String statusLabel,
        Long storageLocationId,
        String storageLocationCode,
        String storageLocationName,
        String captureUrl,
        Long workerId,
        String workerEmployeeNo,
        String workerName,
        LocalDateTime occurredAt,
        LocalDateTime resolvedAt
) {
}

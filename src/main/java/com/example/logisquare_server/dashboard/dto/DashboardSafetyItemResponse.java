package com.example.logisquare_server.dashboard.dto;

import java.time.LocalDateTime;

public record DashboardSafetyItemResponse(
        Long safetyEventId,
        String eventType,
        String locationCode,
        String workerName,
        LocalDateTime occurredAt
) {
}

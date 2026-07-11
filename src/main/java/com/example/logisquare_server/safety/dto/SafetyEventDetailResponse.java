package com.example.logisquare_server.safety.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SafetyEventDetailResponse(
        Long eventId,
        String eventType,
        String eventTypeLabel,
        String sourceType,
        String status,
        String statusLabel,
        String captureUrl,
        BigDecimal confidenceScore,
        Boolean helmetWorn,
        Boolean vestWorn,
        Boolean shoesWorn,
        Long storageLocationId,
        String storageLocationCode,
        String storageLocationName,
        Integer storagePosX,
        Integer storagePosY,
        Long cameraId,
        String cameraCode,
        Long workerId,
        String workerEmployeeNo,
        String workerName,
        Long assignedByUserId,
        String assignedByName,
        LocalDateTime assignedAt,
        Long resolvedByUserId,
        String resolvedByName,
        String resolutionMemo,
        LocalDateTime occurredAt,
        LocalDateTime resolvedAt
) {
}
